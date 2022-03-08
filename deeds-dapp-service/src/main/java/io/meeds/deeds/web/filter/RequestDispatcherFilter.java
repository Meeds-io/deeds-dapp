package io.meeds.deeds.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcherFilter implements Filter {

  private static final List<String> PATHS = Arrays.asList("/", "/snapshot", "/stake", "/deeds", "/farm");

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to start
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    if (PATHS.contains(request.getServletPath())) {
      response.setContentType("text/html; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/view.jsp");
      dispatcher.include(request, response);// NOSONAR
    } else {
      chain.doFilter(request, response);
    }
  }

}
