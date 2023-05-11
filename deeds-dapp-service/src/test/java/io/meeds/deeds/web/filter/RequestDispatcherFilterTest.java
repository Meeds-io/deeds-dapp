package io.meeds.deeds.web.filter;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { RequestDispatcherFilter.class })
class RequestDispatcherFilterTest {

  @Mock
  private HttpServletRequest                       request;

  @Mock
  private HttpServletResponse                      response;

  @Mock
  private RequestDispatcher                        dispatcher;

  @Mock
  private FilterChain                              chain;

  private RequestDispatcherFilter dispatcherFilter;

  @BeforeEach
  void before() throws ServletException {
    dispatcherFilter = new RequestDispatcherFilter() {
      private static final long serialVersionUID = 4367082767921658504L;
    };
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
  }

  @Test
  void testGetContentPage() throws Exception {
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://test/test"));
    when(request.getServletPath()).thenReturn("/test");
    when(request.getParameter("lang")).thenReturn("en");
    dispatcherFilter.doFilter(request, response, chain);
  }
  
}
