Vue.directive('sanitized-html', function (el, binding) {
  const pureHtml = DOMPurify.sanitize(Autolinker.link(binding.value, {
    email: false,
    replaceFn: function (match) {
      switch (match.getType()) {
      case 'url' :
        if (match.getUrl().indexOf(window.location.origin) === 0) {
          return true;
        } else {
          const tag = match.buildTag();
          tag.setAttr('target', '_blank');
          tag.setAttr('rel', 'nofollow noopener noreferrer');
          return tag;
        }
      default :
        return true;
      }
    }
  }), {
    USE_PROFILES: {
      html: true,
      SAFE_FOR_TEMPLATES: true,
      svg: true
    },
    ADD_ATTR: ['target', 'allow', 'scrolling'],
  });
  el.innerHTML = pureHtml;
});
