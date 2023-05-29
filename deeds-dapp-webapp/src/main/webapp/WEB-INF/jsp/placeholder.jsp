<%@page import="io.meeds.deeds.web.utils.Utils"%>
<%
  String buildnumber = Utils.getApplicationBuildNumber();
  String basePath = request.getServletPath().startsWith("/fr/") ? "../" : "./";
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
    <title>Meeds DAO - Deed Tenant</title>
    <meta name="msapplication-TileImage" content="<%=basePath%>static/images/meedsicon.png">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="version" content="<%=buildnumber%>">

    <link rel="preload" href="<%=basePath%>static/fonts/fa-brands-400.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-brands-400.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-regular-400.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-regular-400.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-solid-900.woff2?v=6.2.0" as="font" type="font/woff2" crossorigin>
    <link rel="preload" href="<%=basePath%>static/fonts/fa-solid-900.ttf?v=6.2.0" as="font" type="font/woff2" crossorigin>

    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/vuetify.css?_=v2.6.14">
    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/font-awesome.min.css?_=6.2.0">
    <link rel="preload" as="style" type="text/css" href="<%=basePath%>static/css/deeds.css?_=<%=buildnumber%>">

    <link rel="preload" href="<%=basePath%>static/js/vue.min.js?_=v2.6.14" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/vuetify.min.js?_=v2.6.14" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/vue-i18n.min.js?_=v8.26.7" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/vuex.min.js?_=v3.6.2" as="script" type="text/javascript">
    <link rel="preload" href="<%=basePath%>static/js/tenantPlaceholder.js?_=<%=buildnumber%>" as="script" type="text/javascript">

    <link rel="preload" href="<%=basePath%>static/images/meedsicon.png" as="image" type="image/png">
    <link rel="preload" href="<%=basePath%>static/images/meedsicon.png" as="image" type="image/png">
    <link rel="preload" href="<%=basePath%>static/images/meeds.png" as="image" type="image/png">

    <link rel="icon" href="<%=basePath%>static/images/meedsicon.png" sizes="32x32">
    <link rel="icon" href="<%=basePath%>static/images/meedsicon.png" sizes="192x192">
    <link rel="apple-touch-icon-precomposed" href="<%=basePath%>static/images/meedsicon.png">
    <link href="<%=basePath%>static/css/vuetify.css?_=v2.6.14" rel="stylesheet">
    <link href="<%=basePath%>static/css/font-awesome.min.css?_=6.2.0" rel="stylesheet">
    <link href="<%=basePath%>static/css/deeds.css?_=<%=buildnumber%>" rel="stylesheet">
  </head>
  <body>
    <div id="tenantPlaceholder"></div>
    <script src="<%=basePath%>static/js/vue.min.js?_=v2.7.14"></script>
    <script src="<%=basePath%>static/js/vuetify.min.js?_=v2.6.14"></script>
    <script src="<%=basePath%>static/js/vue-i18n.min.js?_=v8.26.7"></script>
    <script src="<%=basePath%>static/js/vuex.min.js?_=v3.6.2"></script>
    <script src="<%=basePath%>static/js/tenantPlaceholder.js?_=<%=buildnumber%>"></script>
  </body>
</html>