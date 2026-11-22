(function () {
  "use strict";

  document.addEventListener("click", function (event) {
    var thumb = event.target.closest("[data-thumb]");
    if (thumb) {
      var gallery = thumb.closest("[data-gallery]");
      if (!gallery) return;
      var image = gallery.querySelector("[data-gallery-main]");
      if (image) image.src = thumb.getAttribute("data-src") || "";
      gallery.querySelectorAll("[data-thumb]").forEach(function (item) {
        item.classList.toggle("active", item === thumb);
      });
      return;
    }

    var step = event.target.closest("[data-step]");
    if (step) {
      var input = step.closest("[data-quantity]").querySelector("input");
      if (!input) return;
      var value = Number(input.value || 1) + Number(step.getAttribute("data-step"));
      var min = Number(input.getAttribute("min") || 1);
      var max = Number(input.getAttribute("max") || 99);
      input.value = Math.max(min, Math.min(max, value));
    }
  });
})();
