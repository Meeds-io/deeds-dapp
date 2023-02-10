<%@page import="io.meeds.deeds.web.utils.Utils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
  String buildnumber = Utils.getApplicationBuildNumber();
  String fileContent = (String) request.getAttribute("fileContent");
  String extendedHtmlContent = Utils.getExtendedHtmlContent();
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
    <title>Work Metaverse | Meeds DAO</title>
    <meta name="msapplication-TileImage" content="./static/images/meedsicon.png">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="version" content="<%=buildnumber%>">
    <meta name="description" content="Meeds is an employee recognition open source software to engage and reward DAO members. Learn more about our recognition and rewards program.">
    <meta name="description" content="Promote employee recognition &amp; hapiness at work">
    <meta name="robots" content="max-snippet:-1, max-image-preview:large, max-video-preview:-1">
    <link rel="canonical" href="https://www.meeds.io/">
    <meta property="og:locale" content="en_US">
    <meta property="og:type" content="website">
    <meta property="og:title" content="Employee Recognition Software &amp; Employee Rewards Program | Meeds">
    <meta property="og:description" content="Meeds is an employee recognition software to engage and reward employees. Increase employee engagement and retention through a recognition and rewards program.">
    <meta property="og:url" content="https://www.meeds.io/">
    <meta property="og:site_name" content="Meeds">
    <meta property="og:image" content="https://www.meeds.io/wp-content/uploads/2020/05/meeds-association-employee-recognition-software-facebook-preview.png">
    <meta property="og:image:secure_url" content="https://www.meeds.io/wp-content/uploads/2020/05/meeds-association-employee-recognition-software-facebook-preview.png">
    <meta property="og:image:width" content="1200">
    <meta property="og:image:height" content="630">
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:description" content="Meeds is an employee recognition software to engage and reward employees. Increase employee engagement and retention through a recognition and rewards program.">
    <meta name="twitter:title" content="Employee Recognition Software &amp; Employee Rewards Program | Meeds">
    <meta name="twitter:image" content="https://www.meeds.io/wp-content/uploads/2020/05/meeds-association-employee-recognition-software-twitter-preview.png">

    <link rel="preload" as="style" type="text/css" href="./static/css/fonts-roboto.css?_=6.5.95">
    <link rel="preload" as="style" type="text/css" href="./static/css/font-awesome.min.css?_=6.2.0">
    <link rel="preload" as="style" type="text/css" href="./static/css/vuetify.min.css?_=v2.6.12">
    <link rel="preload" as="style" type="text/css" href="./static/css/deeds.css?_=<%=buildnumber%>">
    <link rel="preload" as="style" type="text/css" href="./static/css/vuetify-theme.css?_=v2.6.12_1">

    <link rel="preload" href="./static/fonts/fa-brands-400.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="./static/fonts/fa-brands-400.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="./static/fonts/fa-regular-400.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="./static/fonts/fa-regular-400.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="./static/fonts/fa-solid-900.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="./static/fonts/fa-solid-900.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="./static/fonts/KFOlCnqEu92Fr1MmEU9fBBc4.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="./static/fonts/KFOlCnqEu92Fr1MmWUlfBBc4.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="./static/fonts/KFOmCnqEu92Fr1Mu4mxK.woff2" as="font" type="font/woff2" crossorigin="">

    <link rel="preload" href="./static/js/vue.min.js?_=v2.7.14" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/vue-i18n.min.js?_=v8.26.7" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/vuex.min.js?_=v3.6.2" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/vuetify.min.js?_=v2.6.12" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/bignumber.min.js?_=v9.0.1" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/ethers.umd.min.js?_=5.5.1" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/deeds.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/jdenticon.min.js?_=3.1.1" as="script" type="text/javascript">

    <link rel="preload" href="./static/images/meedsicon.png" as="image" type="image/png">
    <link rel="preload" href="./static/images/meedsicon.png" as="image" type="image/png">
    <link rel="preload" href="./static/images/meeds.png" as="image" type="image/png">

    <link rel="icon" href="./static/images/meedsicon.png" sizes="32x32">
    <link rel="icon" href="./static/images/meedsicon.png" sizes="192x192">
    <link rel="apple-touch-icon-precomposed" href="./static/images/meedsicon.png">
    <link href="./static/css/fonts-roboto.css?_=6.5.95" rel="stylesheet">
    <link href="./static/css/font-awesome.min.css?_=6.2.0" rel="stylesheet">
    <link href="./static/css/vuetify.min.css?_=v2.6.12" rel="stylesheet">
    <link href="./static/css/vuetify-theme.css?_=v2.6.12_1" rel="stylesheet">
    <link href="./static/css/deeds.css?_=<%=buildnumber%>" rel="stylesheet">
  </head>
  <body>
    <div id="deedsApp"></div>

    <% if (StringUtils.isNotBlank(fileContent)) { %>
    <div id="staticContent" style="display: none;">
      <%= fileContent %>
    </div>
    <% } %>

    <% if (StringUtils.isNotBlank(request.getRemoteUser())) { %>
      <input type="hidden" name="login" value="<%=request.getRemoteUser()%>">
    <% } else { %>
      <input type="hidden" name="login">
    <% } %>
    <input type="hidden" name="loginMessage" value="<%=Utils.generateLoginMessage(session)%>">

    <script src="./static/js/vue.min.js?_=v2.7.14"></script>
    <script src="./static/js/vue-i18n.min.js?_=v8.26.7"></script>
    <script src="./static/js/vuex.min.js?_=v3.6.2"></script>
    <script src="./static/js/vuetify.min.js?_=v2.6.12"></script>
    <script src="./static/js/bignumber.min.js?_=v9.0.1"></script>
    <script src="./static/js/ethers.umd.min.js?_=5.5.1"></script>
    <script src="./static/js/deeds.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/jdenticon.min.js?_=3.1.1"></script>

    <%=extendedHtmlContent == null ? "" : extendedHtmlContent%>
  </body>
</html>