package io.meeds.deeds.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(
    name = "RequestDispatcherSerlvet",
    description = "Servlet To dispatch request to index.jsp",
    urlPatterns = { "/", "/snapshot", "/stake", "/deeds", "/farm" },
    loadOnStartup = 1
)
public class RequestDispatcherSerlvet extends HttpServlet {

  private static final long serialVersionUID = -8790234117359459323L;

  @Override
  protected void doGet(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html; charset=UTF-8");
    RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
    dispatcher.include(request, response);// NOSONAR
  }

}
