## 05. Logs / whitelist / FAQ

English | [简体中文](05-audit-faq.zh-CN.md)

### Logs

- **Login logs**: track authentication activities.
- **Operation logs**: track important operations performed through the system.

Suggested workflow when troubleshooting:

- For **401**: check token validity / login state.
- For **403**: check role-api bindings and whitelist rules.

### Whitelist

Whitelist rules can bypass role-api binding checks for matched requests (method + AntPath). Use with caution.

### FAQ

- **I changed `MYSQL_DATABASE` but DB name is unchanged**
  - MySQL init DB is only created on first init; remove volume with `docker compose down -v` (data loss).
- **UI shows menu but API returns 403**
  - Menu permission and API permission are separate; bind the API to the role.

