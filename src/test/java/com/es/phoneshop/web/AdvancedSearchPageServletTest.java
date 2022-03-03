package com.es.phoneshop.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdvancedSearchPageServletTest {
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private ServletConfig config;
  @Mock
  private RequestDispatcher requestDispatcher;
  
  AdvancedSearchPageServlet servlet = new AdvancedSearchPageServlet();

  @Before
  public void setUp() throws Exception {
    servlet.init(config);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  public void testIfErrorInMinPrice() throws ServletException, IOException {
    when(request.getParameter("minPrice")).thenReturn("asd");
    when(request.getParameterMap()).thenReturn(Map.of("minPrice", new String[]{"asd"}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("validationErrors"), any());
  }

  @Test
  public void testIfErrorInMaxPrice() throws ServletException, IOException {
    when(request.getParameter("maxPrice")).thenReturn("asd");
    when(request.getParameterMap()).thenReturn(Map.of("maxPrice", new String[]{"asd"}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("validationErrors"), any());
  }

  @Test
  public void testIfErrorInMinStock() throws ServletException, IOException {
    when(request.getParameter("minStock")).thenReturn("asd");
    when(request.getParameterMap()).thenReturn(Map.of("minStock", new String[]{"asd"}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("validationErrors"), any());
  }

  @Test
  public void testIfMinStockLessThanZero() throws ServletException, IOException {
    when(request.getParameter("minStock")).thenReturn("-2");
    when(request.getParameterMap()).thenReturn(Map.of("minStock", new String[]{"-2"}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("validationErrors"), any());
  }

  @Test
  public void testIfAllParametersAreNull() throws ServletException, IOException {
    servlet.doGet(request, response);
    verify(request).getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testIfAllParametersAreValid() throws ServletException, IOException {
    when(request.getParameter("productCode")).thenReturn("sgs");
    when(request.getParameter("minPrice")).thenReturn("100");
    when(request.getParameter("maxPrice")).thenReturn("1000");
    when(request.getParameter("minStock")).thenReturn("5");
    when(request.getParameterMap()).thenReturn(Map.of("minStock", new String[]{"100"}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("products"), any());
    verify(request).getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testIfProductCodeIsEmpty() throws ServletException, IOException {
    when(request.getParameter("productCode")).thenReturn("");
    when(request.getParameterMap()).thenReturn(Map.of("productCode", new String[]{""}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("products"), any());
    verify(request).getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testIfMinPriceIsEmpty() throws ServletException, IOException {
    when(request.getParameter("minPrice")).thenReturn("");
    when(request.getParameterMap()).thenReturn(Map.of("minPrice", new String[]{""}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("products"), any());
    verify(request).getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testIfMaxPriceIsEmpty() throws ServletException, IOException {
    when(request.getParameter("maxPrice")).thenReturn("");
    when(request.getParameterMap()).thenReturn(Map.of("maxPrice", new String[]{""}));
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("products"), any());
    verify(request).getRequestDispatcher("/WEB-INF/pages/advancedSearch.jsp");
    verify(requestDispatcher).forward(request, response);
  }
}