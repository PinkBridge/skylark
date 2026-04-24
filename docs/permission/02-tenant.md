## 02. Tenant management

English | [简体中文](02-tenant.zh-CN.md)

### Goal

Create a tenant and assign a tenant admin.

### Concepts

- **Tenant**: a logical boundary for data isolation and administration.
- **Tenant admin**: manages users/roles within a tenant.

### Steps (UI)

1. **Open the Admin UI**

- Navigate to the tenant management page.

2. **Create a tenant**

- In the create/edit form, fill in the following fields from top to bottom:
- **Name** (required)
- **System name**
- **Domain** (tenant dedicated domain access, e.g. `xxx.yyy.com`)
- **Logo** (upload an image; UI提示: only one image file)
- **Contact name**
- **Contact phone**
- **Contact email**
- **Address**
- **Status** (e.g. Active / Disabled)
- **Expire time**

3. **Create a tenant role**

- Create a role for the tenant (suggest clear names such as `TENANT_ADMIN`, `TENANT_OPERATOR`, `TENANT_READONLY`).
- Grant **menu permissions**, **API permissions** (see `04-role-menu-api.md`), and **data domains** as needed.

4. **Create a tenant admin**

- Click the action button on the newly created tenant row to create a tenant admin.
- Select the newly created tenant role and set the admin **username** and **password**.

5. **Visit the new tenant domain**

- Visit the new tenant domain and log in with the tenant admin account to manage tenant data.
- Click the **Tenant profile** button and update the information for the tenant you created.
- Click your **user avatar** to update your user profile.

### Verification

- Tenant appears in the tenant list and is enabled.
- Tenant admin can log in and operate within the tenant scope.

### Notes

- If your environment supports tenant-by-domain, ensure the tenant domain mapping is configured.

