# Wallet Service

REST API для управления кошельками.

## API Endpoints

- `POST /api/v1/create` - создать кошелёк
- `POST /api/v1/wallet` - выполнить операцию (пополнение/списание)
- `GET /api/v1/wallets/{id}` - получить баланс

## Запуск

1. Собрать приложение:
`mvn clean package`

2. Запустить в Docker: 
`docker-compose up --build`