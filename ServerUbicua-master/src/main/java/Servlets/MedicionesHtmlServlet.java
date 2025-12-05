package servlets;

import Database.ConectionDDBB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/mediciones")
public class MedicionesHtmlServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String fecha = req.getParameter("fecha");  
        String desde = req.getParameter("desde");   
        String hasta = req.getParameter("hasta");  

        // Checkboxes: si vienen no-null => marcados
        boolean mostrarTemp  = req.getParameter("mostrarTemp")  != null;
        boolean mostrarHum   = req.getParameter("mostrarHum")   != null;
        boolean mostrarUV    = req.getParameter("mostrarUV")    != null;
        boolean mostrarRuido = req.getParameter("mostrarRuido") != null;
        boolean mostrarAire  = req.getParameter("mostrarAire")  != null;

        // Por defecto, si no hay ningún parámetro (primera vez), muestra todo
        boolean sinChecks = req.getParameterMap().isEmpty();
        if (sinChecks) {
            mostrarTemp = mostrarHum = mostrarUV = mostrarRuido = mostrarAire = true;
        }

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder sql = new StringBuilder(
            "SELECT timestamp, temperatura, humedad, radiacion_uv, ruido_db, calidad_aire " +
            "FROM mediciones "
        );

        boolean tieneFecha = fecha != null && !fecha.isBlank();
        boolean tieneRango = (desde != null && !desde.isBlank()) &&
                             (hasta != null && !hasta.isBlank());

        if (tieneRango) {
            sql.append("WHERE DATE(timestamp) BETWEEN ? AND ? ");
        } else if (tieneFecha) {
            sql.append("WHERE DATE(timestamp) = ? ");
        }

        sql.append("ORDER BY timestamp");

        try (Connection con = new ConectionDDBB().obtainConnection(true);
             PreparedStatement st = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (tieneRango) {
                st.setDate(idx++, java.sql.Date.valueOf(desde));
                st.setDate(idx++, java.sql.Date.valueOf(hasta));
            } else if (tieneFecha) {
                st.setDate(idx++, java.sql.Date.valueOf(fecha));
            }

            try (ResultSet rs = st.executeQuery();
                 PrintWriter out = resp.getWriter()) {

                out.println("<!DOCTYPE html>");
                out.println("<html><head>");
                out.println("<meta charset=\"UTF-8\">");
                out.println("<title>Mediciones</title>");
                out.println("</head><body>");

                out.println("<h1>Mediciones</h1>");

                out.println("<form method=\"get\" action=\"/mediciones\">");
                out.println("Fecha (YYYY-MM-DD): <input type=\"text\" name=\"fecha\" value=\"" +
                            (fecha != null ? fecha : "") + "\" />");
                out.println("<br/>Desde (YYYY-MM-DD): <input type=\"text\" name=\"desde\" value=\"" +
                            (desde != null ? desde : "") + "\" />");
                out.println(" Hasta (YYYY-MM-DD): <input type=\"text\" name=\"hasta\" value=\"" +
                            (hasta != null ? hasta : "") + "\" />");

                out.println("<br/><br/>Columnas a mostrar:<br/>");
                out.println("<label><input type=\"checkbox\" name=\"mostrarTemp\" "  +
                            (mostrarTemp  ? "checked" : "") + "> Temperatura</label><br/>");
                out.println("<label><input type=\"checkbox\" name=\"mostrarHum\" "   +
                            (mostrarHum   ? "checked" : "") + "> Humedad</label><br/>");
                out.println("<label><input type=\"checkbox\" name=\"mostrarUV\" "    +
                            (mostrarUV    ? "checked" : "") + "> Radiación UV</label><br/>");
                out.println("<label><input type=\"checkbox\" name=\"mostrarRuido\" " +
                            (mostrarRuido ? "checked" : "") + "> Ruido</label><br/>");
                out.println("<label><input type=\"checkbox\" name=\"mostrarAire\" "  +
                            (mostrarAire  ? "checked" : "") + "> Calidad del aire</label><br/>");

                out.println("<br/><input type=\"submit\" value=\"Filtrar\" />");
                out.println("</form><br/>");

                out.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
                out.println("<tr>");
                out.println("<th>Timestamp</th>");
                if (mostrarTemp)  out.println("<th>Temperatura (°C)</th>");
                if (mostrarHum)   out.println("<th>Humedad (%)</th>");
                if (mostrarUV)    out.println("<th>Radiación UV</th>");
                if (mostrarRuido) out.println("<th>Ruido (dB)</th>");
                if (mostrarAire)  out.println("<th>Calidad aire (ppm)</th>");
                out.println("</tr>");

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td>" + rs.getTimestamp("timestamp") + "</td>");
                    if (mostrarTemp) {
                        out.println("<td>" + rs.getDouble("temperatura") + "</td>");
                    }
                    if (mostrarHum) {
                        out.println("<td>" + rs.getDouble("humedad") + "</td>");
                    }
                    if (mostrarUV) {
                        out.println("<td>" + rs.getDouble("radiacion_uv") + "</td>");
                    }
                    if (mostrarRuido) {
                        out.println("<td>" + rs.getDouble("ruido_db") + "</td>");
                    }
                    if (mostrarAire) {
                        out.println("<td>" + rs.getDouble("calidad_aire") + "</td>");
                    }
                    out.println("</tr>");
                }

                out.println("</table>");
                out.println("</body></html>");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("<h1>Error al consultar mediciones</h1>");
                out.println("<pre>" + e.getMessage() + "</pre>");
            }
        }
    }
}
