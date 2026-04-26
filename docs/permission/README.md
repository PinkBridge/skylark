## Permission Manual

English | [简体中文](README.zh-CN.md)

This manual explains how to **initialize** and **use** the `permission` subsystem as an administrator/tenant admin, including how to create **tenants**, **organizations**, **users**, **roles**, and how to grant **menu/API** permissions.

### Features

- **Multi-tenant management**: create and maintain tenants; supports domain-based access (when tenant-by-domain is enabled).
- **Multi-app integration**: manage multiple applications in one permission system (apps landing, navigation, and unified sign-in integration).
- **OAuth2 single sign-on (SSO)**: provides centralized login and token issuance so business apps can integrate via OAuth2 and reuse authentication/authorization.
- **Organizations & users**: manage organization tree, user accounts, and org membership.
- **Roles & authorization (RBAC)**: bind menus and APIs to roles, and assign roles to users.
- **Data domains (data scope)**: configure role-based data access scope (e.g. org-only / self-only / whole-tenant) for row-level filtering in business modules.
- **Internationalization (i18n)**: the admin UI and business apps can enable language switching (e.g. English/Chinese).
- **Operations**: login/operation logs and whitelist utilities (see the chapters).

### Chapters

- [01. System initialization](01-init.md)
- [02. Tenant management](02-tenant.md)
- [03. Organization & users](03-org-user.md)
- [04. Roles & authorization (Menu / API / RBAC)](04-role-menu-api.md)
- [05. Data domains (data scope)](05-data-domains.md)
- [06. Logs / whitelist / FAQ](05-audit-faq.md)

