package org.jettyissue;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.io.PrintWriter;

class DemoServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("text/html");
    PrintWriter pw=res.getWriter();

    pw.println("<html><body>demo servlet</body></html>");
    pw.close();
  }
}

class ProxyRedirectHandler extends HandlerWrapper {
  private final String proxyBase;

  ProxyRedirectHandler(final String proxyBaseUri) {
    proxyBase = proxyBaseUri;
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    super.handle(target, baseRequest, request, new ProxyRedirectHandlerResponseWrapper(response));
  }

  class ProxyRedirectHandlerResponseWrapper extends HttpServletResponseWrapper {
    ProxyRedirectHandlerResponseWrapper(HttpServletResponse response) {
      super(response);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
      System.out.println("Location: " + location);
      // Logic to re-write the location with proxy-base goes here.
      String newLocation = null;
      if (location != null) {
        newLocation = proxyBase + location;
        System.out.println("New Location: " + newLocation);
      }
      super.sendRedirect(newLocation);
    }
  }
}

public class ProxyRedirectTest {

  private static ServletContextHandler getDemoServetContextHandler() {
    HttpServlet servlet = new DemoServlet();

    ServletContextHandler ctx = new ServletContextHandler();
    ctx.setContextPath("/demo");
    ctx.addServlet(new ServletHolder(servlet), "/*");
    return ctx;
  }

  public static void main(String[] args) throws Exception {
    Server server = new Server(5002);

    ContextHandlerCollection collection = new ContextHandlerCollection();
    collection.addHandler(getDemoServetContextHandler());

    ProxyRedirectHandler proxyRedirectHandler = new ProxyRedirectHandler("/shs");
    proxyRedirectHandler.setHandler(collection);

    server.setHandler(proxyRedirectHandler);

    server.start();
  }
}
