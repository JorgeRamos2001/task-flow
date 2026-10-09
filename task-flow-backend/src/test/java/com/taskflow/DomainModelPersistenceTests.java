package com.taskflow;

import com.taskflow.activity.entity.BoardActivity;
import com.taskflow.boards.entity.Board;
import com.taskflow.boards.entity.BoardColumn;
import com.taskflow.boards.entity.BoardMode;
import com.taskflow.boards.entity.BoardRole;
import com.taskflow.boards.entity.BoardTag;
import com.taskflow.notifications.entity.Notification;
import com.taskflow.notifications.entity.NotificationType;
import com.taskflow.tasks.entity.SubTask;
import com.taskflow.tasks.entity.Task;
import com.taskflow.users.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class DomainModelPersistenceTests {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void newBoardPersistsItsOwnerAndDefaultColumns() {
		User owner = persistUser("owner", "owner@example.com");
		Board board = new Board(owner, "Product Launch", "Launch board", BoardMode.PERSONAL, "#F4C542");

		entityManager.persist(board);
		entityManager.flush();

		assertEquals(1, board.getMembers().size());
		assertEquals(BoardRole.OWNER, board.getMembers().getFirst().getRole());
		assertEquals(List.of("Backlog", "To Do", "In Progress", "Done"),
				board.getColumns().stream().map(BoardColumn::getName).toList());
		assertEquals(4L, entityManager.createQuery(
				"select count(boardColumn) from BoardColumn boardColumn where boardColumn.board.id = :boardId", Long.class)
			.setParameter("boardId", board.getId())
			.getSingleResult());
	}

	@Test
	void databaseRejectsASecondOwnerForOneBoard() {
		User owner = persistUser("owner2", "owner2@example.com");
		User anotherUser = persistUser("member2", "member2@example.com");
		Board board = new Board(owner, "Personal Board", null, BoardMode.PERSONAL, "#F4C542");
		entityManager.persist(board);
		entityManager.flush();

		assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
				INSERT INTO board_members (id, board_id, user_id, role)
				VALUES (?, ?, ?, 'OWNER')
				""", UUID.randomUUID(), board.getId(), anotherUser.getId()));
	}

	@Test
	void taskAssigneeMustBelongToTheSameBoard() {
		User owner = persistUser("owner3", "owner3@example.com");
		User outsider = persistUser("outsider", "outsider@example.com");
		Board board = new Board(owner, "Team Board", null, BoardMode.COLLABORATIVE, "#53C7D8");
		entityManager.persist(board);
		entityManager.flush();

		BoardColumn column = board.getColumns().getFirst();
		Task task = new Task(board, column, owner, outsider, "Invalid assignment", null, null, 0);
		entityManager.persist(task);

		assertThrows(ConstraintViolationException.class, entityManager::flush);
	}

	@Test
	void taskHierarchyPersistsWithBoardScopedTagsAndActivity() {
		User owner = persistUser("owner4", "owner4@example.com");
		User assignee = persistUser("assignee", "assignee@example.com");
		Board board = new Board(owner, "Research Board", "Research tasks", BoardMode.COLLABORATIVE, "#53C7D8");
		board.addMember(assignee, BoardRole.MEMBER);
		BoardTag tag = board.addTag("Research", "#F2A5C7");
		entityManager.persist(board);
		entityManager.flush();

		Task task = new Task(board, board.getColumns().getFirst(), owner, assignee, "Interview users",
				"Prepare the first round", Instant.parse("2026-10-20T23:59:00Z"), 0);
		SubTask subTask = task.addSubTask("Draft interview questions", 0);
		subTask.setCompleted(true, Instant.parse("2026-10-09T12:00:00Z"));
		task.addTag(tag);
		task.addComment(assignee, "The questions are ready for review.");
		entityManager.persist(task);
		entityManager.flush();

		entityManager.persist(new BoardActivity(board, assignee, assignee.getName(), "TASK_MOVED", "TASK",
				task.getId(), Map.of("column", "To Do")));
		entityManager.persist(new Notification(assignee, board, task, NotificationType.BOARD_ACTIVITY,
				"Task updated", "Interview users moved to To Do", "task-moved:" + task.getId()));
		entityManager.flush();

		assertEquals(1L, entityManager.createQuery(
				"select count(subTask) from SubTask subTask where subTask.task.id = :taskId", Long.class)
			.setParameter("taskId", task.getId()).getSingleResult());
		assertEquals(1L, entityManager.createQuery(
				"select count(comment) from Comment comment where comment.task.id = :taskId", Long.class)
			.setParameter("taskId", task.getId()).getSingleResult());
		assertEquals(1L, entityManager.createQuery(
				"select count(taskTag) from TaskTag taskTag where taskTag.task.id = :taskId and taskTag.tag.id = :tagId",
				Long.class).setParameter("taskId", task.getId()).setParameter("tagId", tag.getId()).getSingleResult());
		assertEquals(1L, entityManager.createQuery(
				"select count(activity) from BoardActivity activity where activity.board.id = :boardId",
				Long.class).setParameter("boardId", board.getId()).getSingleResult());
	}

	@Test
	void userIdentifiersAreNormalizedBeforePersistence() {
		User user = new User("Case Sensitive Name", "Mixed.User", "Person@Example.com", "$2a$12$test-hash-placeholder");

		assertEquals("mixed.user", user.getUsername());
		assertEquals("person@example.com", user.getEmail());
	}

	@Test
	void usernameIsUniqueRegardlessOfInputCapitalization() {
		persistUser("planner", "planner-one@example.com");
		User duplicate = new User("Another User", "PLANNER", "planner-two@example.com", "$2a$12$test-hash-placeholder");
		entityManager.persist(duplicate);

		assertThrows(ConstraintViolationException.class, entityManager::flush);
	}

	@Test
	void emailIsUniqueRegardlessOfInputCapitalization() {
		persistUser("email-owner", "person@example.com");
		User duplicate = new User("Another User", "email-duplicate", "Person@Example.com", "$2a$12$test-hash-placeholder");
		entityManager.persist(duplicate);

		assertThrows(ConstraintViolationException.class, entityManager::flush);
	}

	private User persistUser(String username, String email) {
		User user = new User(username, username, email, "$2a$12$test-hash-placeholder");
		entityManager.persist(user);
		entityManager.flush();
		return user;
	}
}
