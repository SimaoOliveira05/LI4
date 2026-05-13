# Vue 3 + TypeScript + Vite

This template should help get you started developing with Vue 3 and TypeScript in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about the recommended Project Setup and IDE Support in the [Vue Docs TypeScript Guide](https://vuejs.org/guide/typescript/overview.html#project-setup).


# Running this project

In order to run this project you have to run:

```bash
docker compose up --build
```

Open a browser and test via:

```bash
localhost:3000
```

Stop running containers via:

```bash
docker compose stop
```

Resume your containers via:

```bash
docker compose start
```

Clean your containers via:

```bash
docker compose down -v
```

Warning: This is going to clear your database.