# Postman for Vortex ETL API

This directory contains the Postman collection and environment files for interacting with the Vortex ETL API locally.

## Getting Started

1.  **Install Postman:** If you don't have it, [download it here](https://www.postman.com/downloads/).

2.  **Import Files:**
    *   Open Postman and go to `File > Import...`.
    *   Drag and drop both `Vortex_ETL_API.postman_collection.json` and `Vortex_ETL_Local.postman_environment.json` into the import window.

3.  **Select the Environment:**
    *   In the top-right corner of Postman, select the **"Vortex ETL Local"** environment from the dropdown. This will activate the `{{baseUrl}}` variable.

4.  **Authenticate:**
    *   Navigate to the **Authentication > Login** request within the collection.
    *   Go to the "Body" tab and enter valid credentials. Default test admin credentials are provided in the environment.
    *   Click **Send**.
    *   The request will automatically save the JWT to the `{{authToken}}` environment variable. You are now authenticated for all other requests in the collection.

You are now ready to make authenticated requests to the API!
