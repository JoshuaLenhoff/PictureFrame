Docker starten:
1. docker compose up -d
2. docker compose ps
3. docker compose exec postgres psql -U pictureframe -d pictureframe -c "SELECT version();"