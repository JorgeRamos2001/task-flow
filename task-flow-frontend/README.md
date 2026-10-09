# TaskFlow Frontend

Angular client for TaskFlow V1. API contracts are maintained in `../docs/api/openapi.yaml`; product and authorization decisions are in `../docs/architecture/`.

## Requirements

- Node.js 22.22.3 or newer in the Node 22 line.
- npm 10 or newer.

## Commands

```bash
npm ci
npm start
npm test -- --watch=false
npm run build
```

The local dev server runs at <http://localhost:4200>. Backend/API integration and the product UI are added feature-by-feature after their contracts are agreed.
