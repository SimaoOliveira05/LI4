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