## 05. Data domains (data scope)

English | [简体中文](05-data-domains.zh-CN.md)

### Goal

Define **data scope** rules (“who can see which rows”) and bind them to roles.

### Concepts

- **Data domain**: a reusable data-scope rule. Roles bind data domains; APIs that support data-scope will be filtered accordingly.
- **Org-based scope**: many scopes depend on a user's organization (`org_id`) and the org tree.

### Scope types (common)

Depending on your environment, you may see some of the following types:

- **ALL**: all data (no restriction).
- **TENANT**: all data within the current tenant.
- **ORG_ALL / ORG_AND_CHILD / ORG_ONLY**: organization-based scopes (self org, include children, etc.).
- **SELF**: only the current user's own data.
- **CUSTOM**: custom SQL (use carefully).

### Steps (UI)

Log in to the Admin UI.

1. **Create a data domain**

- Go to **System / Data domains**.
- Create a new data domain and choose the scope type.
- (Optional) For org-based scopes, ensure your organization tree is ready first (see `03-org-user.md`).

2. **Bind data domains to a role**

- Go to **Role management**.
- Open the role and bind one or more data domains.

3. **Assign the role to users**

- Go to **User management** and assign the role to target users.

### Verification

- Log in as the target user and open a list page that is protected by data-scope.
- Confirm the returned data matches the expected scope.

### Notes

- A data domain only takes effect on endpoints that have integrated data-scope filtering.
- If a user has multiple roles with data domains, the final scope is determined by the backend policy (commonly “union” or “most permissive”). Verify in your environment.
- Use **CUSTOM SQL** only when necessary and review it carefully to avoid security risks.

