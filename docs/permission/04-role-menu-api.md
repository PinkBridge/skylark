## 04. Roles & authorization (Menu / API / RBAC)

English | [简体中文](04-role-menu-api.zh-CN.md)

### Goal

Create roles and grant permissions so users can see menus and access APIs.

### Steps (UI)

1. **Create a role**
- Use clear naming (e.g. `TENANT_ADMIN`, `OPS_READONLY`).

2. **Grant menu permissions**
- Bind menus to the role.
- Verify “My menu tree” reflects the granted menus.

3. **Grant API permissions (RBAC)**
- Ensure required APIs exist (method + path).
- Bind APIs to the role.

4. **Assign role to users**
- Bind one or more roles to a user.

### Verification

- [ ] User can see the expected menus
- [ ] Calling protected endpoints returns 200 (not 403)

### Notes

- API matching uses **HTTP method + Ant-style path pattern**. Avoid overly broad patterns unless necessary.

