package io.meeds.deeds.web.filter;

import static io.meeds.deeds.web.filter.RequestDispatcherFilter.LAST_MODIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.meeds.deeds.web.utils.EnvironmentService;

@SpringBootTest(classes = {
    RequestDispatcherFilter.class,
    EnvironmentService.class,
})
@TestPropertySource(properties = {
    "meeds.deed.hostEnvironment=production",
})
class RequestDispatcherFilterTest {

  @Mock
  private HttpServletRequest      request;

  @Mock
  private HttpServletResponse     response;

  @Mock
  private RequestDispatcher       dispatcher;

  @Mock
  private FilterChain             chain;

  private RequestDispatcherFilter dispatcherFilter;

  @BeforeEach
  void before() throws ServletException {
    dispatcherFilter = new RequestDispatcherFilter() {
      private static final long serialVersionUID = 4367082767921658504L;
    };
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
  }

  @Test
  void testGetNotFoundPage() throws Exception {
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://test/test"));
    when(request.getServletPath()).thenReturn("/test");
    when(request.getParameter("lang")).thenReturn("en");
    dispatcherFilter.doFilter(request, response, chain);
    verify(response, never()).setStatus(anyInt());
    verify(response, never()).setHeader(any(), any());
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void testGetStaticPage() throws Exception {
    String servletPath = "/tour";
    String lang = "en";
    String i18NContent = """
        metadata.tour.pageDescription=Test Label
        metadata.tour.imageAlt=Image Alt
        metadata.tour.twitterTitle=Twitter Title
        metadata.tour.pageTitle=Page Title
        """;
    String metadataContent = "#{metadata.tour.pageDescription}-${lang}";

    when(request.getRequestURL()).thenReturn(new StringBuffer("http://test" + servletPath));
    when(request.getServletPath()).thenReturn(servletPath);
    when(request.getParameter("lang")).thenReturn(lang);
    ServletContext servletContext = mock(ServletContext.class);
    when(request.getServletContext()).thenReturn(servletContext);
    when(servletContext.getResourceAsStream("/static/i18n/messages_" + lang
        + ".properties")).thenReturn(new ByteArrayInputStream(i18NContent.getBytes()));
    when(servletContext.getResourceAsStream("/WEB-INF/metadata" + servletPath
        + ".html")).thenReturn(new ByteArrayInputStream(metadataContent.getBytes()));

    dispatcherFilter.doFilter(request, response, chain);

    verify(request, times(1)).setAttribute("isStaticPath", true);
    verify(response, times(1)).setContentType("text/html; charset=UTF-8");
    verify(response, times(1)).setDateHeader("Last-Modified", LAST_MODIFIED);
    verify(response, times(1)).setHeader("Cache-Control", "public,must-revalidate");
    verify(response, times(1)).setHeader(eq("etag"), any());
    verify(request, times(1)).setAttribute(eq("pageHeaderMetadatas"), argThat(contentObject -> {
      String content = (String) contentObject;
      assertEquals(metadataContent.replace("#{metadata.tour.pageDescription}", "Test Label").replace("${lang}", lang), content);
      return true;
    }));

    verify(chain, never()).doFilter(request, response);
    verify(dispatcher, times(1)).include(request, response);
  }

}
