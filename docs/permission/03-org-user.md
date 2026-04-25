## 03. Manage tenant orgs, users, roles

English | [简体中文](03-org-user.zh-CN.md)

### Goal

Build an organization structure in a tenant, create user accounts, and configure business roles and grants.

### Concepts

- **Organization**: a tenant-scoped hierarchy (e.g. company, department, team) used to model reporting structure. Each node can have a parent, forming a tree.
- **User**: a login account belonging to exactly one tenant. A user may optionally be assigned to one organization; that link is often used for administration and **data scope** (see `04-role-menu-api.md`).
- **Role**: a set of permissions in a tenant, used to grant users access to menus and APIs (see `04-role-menu-api.md`).

### Steps (UI)

Log in to the Admin UI as a tenant admin.

1. **Create organizations**

- Plan your org tree based on business needs (e.g. Company / BU / Department).
- Go to **Organization management** and create org nodes. Verify the hierarchy is correct.

2. **Create business roles**

- Plan roles based on your business needs.
- Go to **Role management** and create roles.
- Bind **APIs** (you may select all) and bind **menus** (select menus based on business needs).

3. **Create users**

- Create users and set enabled/disabled status, and select the roles and organizations you created.
- Send the account information to the actual users.

4. **Password operations**

- Admin resets a user's password.
- User changes their password after login.

### Verification

- Org tree is displayed correctly
- Users list shows the new user
- Disabled users cannot log in

