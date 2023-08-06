<%@page import="io.meeds.dapp.web.utils.Utils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
  String buildnumber = Utils.getApplicationBuildNumber();
  boolean isProduction = Utils.isProductionEnvironment();
  Boolean staticPage = (Boolean) request.getAttribute("isStaticPath");
  String lang = (String) request.getAttribute("lang");
  String pageName = (String) request.getAttribute("pageName");
  String basePath = lang.equals("en") || pageName.equals("home") ? "./" : "../";
%>
<!DOCTYPE html>
<html lang="<%=lang%>"> 
  <head>
    <% if (isProduction) { %>
    <!-- Google Tag Manager -->
    <script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
    new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
    j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
    'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
    })(window,document,'script','dataLayer','GTM-WZ644S7');</script>
    <!-- End Google Tag Manager -->
    <% } %>

    <%= request.getAttribute("pageHeaderMetadatas") %>

    <% if (isProduction) { %>
    <meta name="robots" content="max-snippet:-1, max-image-preview:large, max-video-preview:-1">
    <% } else {%>
    <meta name="robots" content="noindex">
    <% } %>
    <meta name="version" content="<%=buildnumber%>">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/fonts-roboto.css?_=6.5.95">
    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/font-awesome.min.css?_=6.2.0">
    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/vuetify.css?_=v2.6.14">
    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/deeds.css?_=<%=buildnumber%>">

    <link rel="preload" href="<%=basePath%>static/fonts/fa-brands-400.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-brands-400.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-regular-400.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-regular-400.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-solid-900.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-solid-900.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/KFOlCnqEu92Fr1MmEU9fBBc4.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="<%=basePath%>static/fonts/KFOlCnqEu92Fr1MmWUlfBBc4.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="<%=basePath%>static/fonts/KFOmCnqEu92Fr1Mu4mxK.woff2" as="font" type="font/woff2" crossorigin="">

    <link rel="preload" href="<%=basePath%>static/js/vue.min.js?_=v2.7.14" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/vue-i18n.min.js?_=v8.26.7" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/vuex.min.js?_=v3.6.2" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/vuetify.min.js?_=v2.6.14" as="script" type="text/javascript">
    <% if (staticPage != null && !staticPage) { %>
      <link rel="preload" href="<%=basePath%>static/js/bignumber.min.js?_=v9.0.1" as="script" type="text/javascript">
      <link rel="preload" href="<%=basePath%>static/js/ethers.umd.min.js?_=5.5.1" as="script" type="text/javascript">
      <link rel="preload" href="<%=basePath%>static/js/jdenticon.min.js?_=3.1.1" as="script" type="text/javascript">
      <link rel="preload" href="<%=basePath%>static/js/purifyGRP.js?_=2.3.8" as="script" type="text/javascript">
      <link rel="preload" href="<%=basePath%>static/js/dapp.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <% } else { %>
      <link rel="preload" href="<%=basePath%>static/js/static.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <% } %>

    <link rel="preload" href="<%=basePath%>static/images/meeds.png" as="image" type="image/png">

    <link rel="icon" href="<%=basePath%>static/images/meedsicon.png" sizes="32x32">
    <link rel="icon" href="<%=basePath%>static/images/meedsicon.png" sizes="192x192">
    <link rel="apple-touch-icon-precomposed" href="<%=basePath%>static/images/meedsicon.png">
  </head>
  <body>
    <link href="<%=basePath%>static/css/fonts-roboto.css?_=6.5.95" rel="stylesheet">
    <link href="<%=basePath%>static/css/font-awesome.min.css?_=6.2.0" rel="stylesheet">
    <link href="<%=basePath%>static/css/vuetify.css?_=v2.6.14" rel="stylesheet">
    <link href="<%=basePath%>static/css/deeds.css?_=<%=buildnumber%>" rel="stylesheet">

    <% if (isProduction) { %>
    <!-- Google Tag Manager (noscript) -->
    <noscript>
      <iframe src="https://www.googletagmanager.com/ns.html?id=GTM-WZ644S7"
      height="0" width="0" style="display:none;visibility:hidden">
      </iframe>
    </noscript>
    <!-- End Google Tag Manager (noscript) -->
    <!-- Cookie Consent by FreePrivacyPolicy.com https://www.FreePrivacyPolicy.com -->
    <script type="text/javascript" src="//www.freeprivacypolicy.com/public/cookie-consent/4.1.0/cookie-consent.js" charset="UTF-8"></script>
    <script type="text/javascript" charset="UTF-8">
      document.addEventListener('DOMContentLoaded', function () {
        cookieconsent.run({"notice_banner_type":"simple","consent_type":"express","palette":"light","language":"en","page_load_consent_levels":["strictly-necessary"],"notice_banner_reject_button_hide":false,"preferences_center_close_button_hide":false,"page_refresh_confirmation_buttons":false,"website_name":"Meeds","website_privacy_policy_url":"https://www.meeds.io/legals#cookiePolicy"});
      });
    </script>
    <!-- Google analytics -->
    <script type="text/plain" data-cookie-consent="tracking">(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
    new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
    j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
    'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
    })(window,document,'script','dataLayer','GTM-WZ644S7');</script>
    <!-- end of Google analytics-->
    <noscript>Cookie Consent by <a href="https://www.freeprivacypolicy.com/">Free Privacy Policy Generator</a></noscript>
    <!-- End Cookie Consent by FreePrivacyPolicy.com https://www.FreePrivacyPolicy.com -->
    <% } %>
    <div id="deedsApp" class="<%= staticPage != null && !staticPage ? "dapp-page" : "" %>"></div>

    <% if (StringUtils.isNotBlank(request.getRemoteUser())) { %>
      <input type="hidden" name="login" value="<%=request.getRemoteUser()%>">
    <% } else { %>
      <input type="hidden" name="login">
    <% } %>
    <input type="hidden" name="loginMessage" value="<%=Utils.generateLoginMessage(session)%>">
    <input type="hidden" name="pageName" value="<%=pageName%>">

    <script id="vue" src="<%=basePath%>static/js/vue.min.js?_=v2.7.14"></script>
    <script id="vue-i18n" src="<%=basePath%>static/js/vue-i18n.min.js?_=v8.26.7"></script>
    <script id="vuex" src="<%=basePath%>static/js/vuex.min.js?_=v3.6.2"></script>
    <script id="vuetify" src="<%=basePath%>static/js/vuetify.min.js?_=v2.6.14"></script>
    <% if (staticPage != null && !staticPage) { %>
      <script id="bignumber" src="<%=basePath%>static/js/bignumber.min.js?_=v9.0.1"></script>
      <script id="ethers" src="<%=basePath%>static/js/ethers.umd.min.js?_=5.5.1"></script>
      <script id="jdenticon" src="<%=basePath%>static/js/jdenticon.min.js?_=3.1.1"></script>
      <script id="jdenticon" src="<%=basePath%>static/js/purifyGRP.js?_=2.3.8"></script>
      <script id="deeds" src="<%=basePath%>static/js/dapp.js?_=<%=buildnumber%>"></script>
    <% } else { %>
      <script id="static" src="<%=basePath%>static/js/static.js?_=<%=buildnumber%>"></script>
    <% } %>

  </body>
</html>