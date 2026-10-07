# Commit Convention Skill

## Формат коммитов

Все коммиты должны следовать формату:
```
<PREFIX>: <description>
```

## Префиксы

| Префикс | Описание |
|---------|----------|
| `ADD` | Добавление новых файлов/функционала |
| `INIT` | Инициализация проекта/модуля |
| `UPDATE` | Обновление существующего кода/конфигов |
| `REMOVE` | Удаление файлов/кода |
| `FIX` | Исправление багов |
| `REFACTOR` | Рефакторинг без изменения поведения |
| `CHORE` | Рутинные задачи (деплой, конфиги, зависимости) |

## Примеры

```
ADD: user authentication module with JWT
INIT: NestJS project structure
UPDATE: product validation rules
REMOVE: deprecated cart endpoint
FIX: race condition in checkout
REFACTOR: extract discount calculation to service
CHORE: update dependencies
```

## Правила

1. Описание на английском языке
2. Первый символ заглавный
3. Без точки в конце
4. Максимально лаконично (до 72 символов)