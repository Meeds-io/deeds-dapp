<%@page import="io.meeds.deeds.web.utils.Utils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
  String buildnumber = Utils.getApplicationBuildNumber();
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Meeds DAO</title>
    <meta name="msapplication-TileImage" content="./static/images/meedsicon.png">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Security-Policy" content="script-src 'self' 'unsafe-eval';
                                                        style-src 'self';
                                                        font-src 'self';
                                                        img-src 'self' storage.googleapis.com">

    <link rel="preload" as="style" type="text/css" href="./static/css/fonts-roboto.css?_=<%=buildnumber%>">
    <link rel="preload" as="style" type="text/css" href="./static/css/materialdesignicons.min.css?_=<%=buildnumber%>">
    <link rel="preload" as="style" type="text/css" href="./static/css/vuetify.min.css?_=<%=buildnumber%>">
    <link rel="preload" as="style" type="text/css" href="./static/css/deeds.css?_=<%=buildnumber%>">
    <link rel="preload" as="style" type="text/css" href="./static/css/vuetify-theme.css?_=<%=buildnumber%>">

    <link rel="preload" href="./static/fonts/KFOlCnqEu92Fr1MmEU9fBBc4.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="./static/fonts/KFOlCnqEu92Fr1MmWUlfBBc4.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="./static/fonts/KFOmCnqEu92Fr1Mu4mxK.woff2" as="font" type="font/woff2" crossorigin="">
    <link rel="preload" href="./static/fonts/materialdesignicons-webfont.woff2?v=6.5.95" as="font" type="font/woff2" crossorigin>

    <link rel="preload" href="./static/js/vue.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/vuetify.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/vue-i18n.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/vuex.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/echarts.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/bignumber.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/ethers.umd.min.js?_=<%=buildnumber%>" as="script" type="text/javascript">
    <link rel="preload" href="./static/js/deeds.js?_=<%=buildnumber%>" as="script" type="text/javascript">

    <link rel="preload" href="./static/images/meedsicon.png" as="image" type="image/png">
    <link rel="preload" href="./static/images/meedsicon.png" as="image" type="image/png">
    <link rel="preload" href="./static/images/meeds.png" as="image" type="image/png">

    <link rel="icon" href="./static/images/meedsicon.png" sizes="32x32">
    <link rel="icon" href="./static/images/meedsicon.png" sizes="192x192">
    <link rel="apple-touch-icon-precomposed" href="./static/images/meedsicon.png">
    <link href="./static/css/fonts-roboto.css?_=<%=buildnumber%>" rel="stylesheet">
    <link href="./static/css/materialdesignicons.min.css?_=<%=buildnumber%>" rel="stylesheet">
    <link href="./static/css/vuetify.min.css?_=<%=buildnumber%>" rel="stylesheet">
    <link href="./static/css/deeds.css?_=<%=buildnumber%>" rel="stylesheet">
    <link href="./static/css/vuetify-theme.css?_=<%=buildnumber%>" rel="stylesheet">
  </head>
  <body>
    <div id="deedsApp"></div>
    <% if (StringUtils.isNotBlank(request.getRemoteUser())) { %>
      <input type="hidden" name="login" value="<%=request.getRemoteUser()%>">
    <% } else { %>
      <input type="hidden" name="login">
    <% } %>
    <input type="hidden" name="loginMessage" value="<%=Utils.generateLoginMessage(session)%>">
    <script src="./static/js/vue.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/vuetify.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/vue-i18n.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/vuex.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/echarts.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/bignumber.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/ethers.umd.min.js?_=<%=buildnumber%>"></script>
    <script src="./static/js/deeds.js?_=<%=buildnumber%>"></script>
  </body>
</html>
