(function () {
  "use strict";

  window.addEventListener("load", function () {
    window.ui = SwaggerUIBundle({
      url: "/api/docs-json",
      dom_id: "#swagger-ui",
      deepLinking: true,
      persistAuthorization: true,
      tagsSorter: "alpha",
      operationsSorter: "alpha",
      presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIStandalonePreset
      ],
      layout: "StandaloneLayout"
    });
  });
})();
