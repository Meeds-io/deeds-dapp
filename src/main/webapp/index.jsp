<%@page import="io.meeds.deeds.web.utils.Utils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Meeds DAO</title>
    <meta name="msapplication-TileImage" content="/dapp/images/favicon.png">
    <meta http-equiv="Content-Security-Policy" content="script-src 'self' https://cdn.jsdelivr.net 'unsafe-eval';
                                                        style-src 'self' https://fonts.gstatic.com/ https://fonts.googleapis.com/ https://cdn.jsdelivr.net/ ;
                                                        font-src https://fonts.gstatic.com https://cdn.jsdelivr.net ;
                                                        img-src 'self'">
    <link rel="icon" href="/dapp/images/favicon.png" sizes="32x32">
    <link rel="icon" href="/dapp/images/favicon.png" sizes="192x192">
    <link rel="apple-touch-icon-precomposed" href="/dapp/images/favicon.png">
    <link href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700,900" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/@mdi/font@6.x/css/materialdesignicons.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/vuetify@2.5.14/dist/vuetify.min.css" rel="stylesheet">
    <link href="/dapp/css/deeds.css" rel="stylesheet">
    <link href="/dapp/css/vuetify-theme.css" rel="stylesheet">
  </head>
  <body>
    <div id="deedsApp"></div>
    <% if (StringUtils.isNotBlank(request.getRemoteUser())) { %>
      <input type="hidden" name="login" value="<%=request.getRemoteUser()%>">
    <% } else { %>
      <input type="hidden" name="login">
    <% } %>
    <input type="hidden" name="loginMessage" value="<%=Utils.generateLoginMessage(session)%>">
    <script src="https://cdn.jsdelivr.net/npm/vue@2.6.14/dist/vue.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/vuetify@2.5.14/dist/vuetify.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/vue-i18n@8.26.7/dist/vue-i18n.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/vuex@3.6.2/dist/vuex.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/echarts@5.2.2/dist/echarts.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bignumber.js@9.0.1/bignumber.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/ethers@5.5.1/dist/ethers.umd.min.js"></script>
    <script src="/dapp/js/deeds.js"></script>
  </body>
</html>
