/* 
 * REST API helper for Alpine.js
 * provided by Hidekazu Kubota under the Apache License 2.0
 */

// Each module version should match the versions
// specified in the dependencies section of your pom.xml file.
import axios from './webjars/axios/1.7.7/dist/esm/axios.js';
import Alpine from './webjars/alpinejs/3.14.3/dist/module.esm.js';

/**
 * Initialization function. Must be called first.
 * @param {string} rootEndpointURL - URL of the root endpoint
 * @param {string} csrfToken - CSRF token
 */
const start = (rootEndpointURL, csrfToken) => {
    delete rest.start;

    // Alpine.js store. Accessible as $successes and $errors from the Alpine components in the HTML.
    Alpine.store('successes', []);
    Alpine.store('errors', []);

    const client = axios.create({
        baseURL: rootEndpointURL,
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
    });

    const showError = (e, defaultErrorMessage = 'Error') => {
        // The response body can be obtained from e.response.data in the Axios error object.
        // Here, we assume that the server returns the following body for a 400 error:
        // {type: 'constraint_error', errors: ['Error 1', 'Error 2', ...] }
        if (e.response?.data?.type == 'constraint_error' && e.response?.data?.errors) {
            Alpine.store('errors').push(...e.response.data.errors);
        }
        // Other error responses
        else if (e.response) {
            Alpine.store('errors').push(defaultErrorMessage + ': ' + e.response.status + " " + e.response.statusText);
        }
        // If e.response is not present, the error occurred on the client side
        else {
            Alpine.store('errors').push(defaultErrorMessage + ': ' + e.message);
        }
    };

    /**
     * Returns a function to call the REST API with the specified method.
     * @param {string} method - The HTTP method to use ('get', 'post', 'put', 'delete', etc.).
     */
    const restFetch = (method) =>
        /**
         * Common process for REST API calls
         * Adds messages corresponding to the result in $successes and $errors, and returns the response.
         *
         * @param {string} endpoint - URL path after the root endpoint
         * @param {Object} options - Request options.
         * @param {Object} options.param - Object to send as JSON.
         * @param {string} [options.success] - Message on success.
         * @param {string} [options.error] - Default message on error.
         * @returns {Promise<Object>} An object containing the response status and data.
         * @returns {number} return.status - HTTP response status.
         * @returns {Object|null} return.data - Response data or null (on error).
         */
        async (endpoint, {param, success, error}) => {
            Alpine.store('successes').length = 0;
            Alpine.store('errors').length = 0;
            let status;
            return client[method](endpoint, param)
                .then(res => {
                    status = res.status;
                    return res.data;
                })
                .then(receivedJson => {
                    if (success) Alpine.store('successes').push(success)
                    console.debug(receivedJson);
                    return {status, data: receivedJson};
                })
                .catch(e => {
                    showError(e, error);
                    return {status: e.response.status, data: null};
                });
        };
    Alpine.magic('get', () => restFetch('get'));
    Alpine.magic('post', () => restFetch('post'));
    Alpine.magic('put', () => restFetch('put'));
    Alpine.magic('delete', () => restFetch('delete'));
    Alpine.magic('rest', () => method => restFetch(method));

    window.Alpine = Alpine;
    Alpine.start();
};

const rest = {
    start,
};
export default rest;
