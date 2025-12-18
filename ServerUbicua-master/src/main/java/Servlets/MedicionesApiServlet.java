package servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import Database.ConectionDDBB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

@WebServlet("/api/mediciones")
public class MedicionesApiServlet extends HttpServlet {

    private java.sql.Date parseFechaDdMmYyyy(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            String[] partes = texto.split("-");
            if (partes.length != 3) return null;
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int anyo = Integer.parseInt(partes[2]);
            java.time.LocalDate ld = java.time.LocalDate.of(anyo, mes, dia);
            return java.sql.Date.valueOf(ld);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String fecha = req.getParameter("fecha");
        String desde = req.getParameter("desde");
        String hasta = req.getParameter("hasta");

        // Filtros por valor
        String tempMin  = req.getParameter("tempMin");
        String tempMax  = req.getParameter("tempMax");
        String humMin   = req.getParameter("humMin");
        String humMax   = req.getParameter("humMax");
        String ruidoMin = req.getParameter("ruidoMin");
        String ruidoMax = req.getParameter("ruidoMax");
        String uvMin    = req.getParameter("uvMin");
        String uvMax    = req.getParameter("uvMax");
        String aireMin  = req.getParameter("aireMin");
        String aireMax  = req.getParameter("aireMax");

        boolean tieneTempMin  = tempMin  != null && !tempMin.isBlank();
        boolean tieneTempMax  = tempMax  != null && !tempMax.isBlank();
        boolean tieneHumMin   = humMin   != null && !humMin.isBlank();
        boolean tieneHumMax   = humMax   != null && !humMax.isBlank();
        boolean tieneRuidoMin = ruidoMin != null && !ruidoMin.isBlank();
        boolean tieneRuidoMax = ruidoMax != null && !ruidoMax.isBlank();
        boolean tieneUvMin    = uvMin    != null && !uvMin.isBlank();
        boolean tieneUvMax    = uvMax    != null && !uvMax.isBlank();
        boolean tieneAireMin  = aireMin  != null && !aireMin.isBlank();
        boolean tieneAireMax  = aireMax  != null && !aireMax.isBlank();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonArray resultados = new JsonArray();

        StringBuilder sql = new StringBuilder(
            "SELECT id, timestamp, temperatura, humedad, radiacion_uv, ruido_db, calidad_aire " +
            "FROM mediciones "
        );

        boolean tieneFecha = fecha != null && !fecha.isBlank();
        boolean tieneRango = (desde != null && !desde.isBlank()) &&
                             (hasta != null && !hasta.isBlank());

        boolean whereUsado = false;

        if (tieneRango) {
            sql.append("WHERE DATE(timestamp) BETWEEN ? AND ? ");
            whereUsado = true;
        } else if (tieneFecha) {
            sql.append("WHERE DATE(timestamp) = ? ");
            whereUsado = true;
        }

        if (tieneTempMin) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("temperatura >= ? ");
            whereUsado = true;
        }
        if (tieneTempMax) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("temperatura <= ? ");
            whereUsado = true;
        }
        if (tieneHumMin) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("humedad >= ? ");
            whereUsado = true;
        }
        if (tieneHumMax) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("humedad <= ? ");
            whereUsado = true;
        }
        if (tieneRuidoMin) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("ruido_db >= ? ");
            whereUsado = true;
        }
        if (tieneRuidoMax) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("ruido_db <= ? ");
            whereUsado = true;
        }
        if (tieneUvMin) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("radiacion_uv >= ? ");
            whereUsado = true;
        }
        if (tieneUvMax) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("radiacion_uv <= ? ");
            whereUsado = true;
        }
        if (tieneAireMin) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("calidad_aire >= ? ");
            whereUsado = true;
        }
        if (tieneAireMax) {
            sql.append(whereUsado ? "AND " : "WHERE ");
            sql.append("calidad_aire <= ? ");
            whereUsado = true;
        }

        sql.append("ORDER BY timestamp");

        try (Connection con = new ConectionDDBB().obtainConnection(true);
             PreparedStatement st = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (tieneRango) {
                java.sql.Date desdeSql = parseFechaDdMmYyyy(desde);
                java.sql.Date hastaSql = parseFechaDdMmYyyy(hasta);
                st.setDate(idx++, desdeSql);
                st.setDate(idx++, hastaSql);
            } else if (tieneFecha) {
                java.sql.Date fechaSql = parseFechaDdMmYyyy(fecha);
                st.setDate(idx++, fechaSql);
            }

            if (tieneTempMin)  st.setDouble(idx++, Double.parseDouble(tempMin));
            if (tieneTempMax)  st.setDouble(idx++, Double.parseDouble(tempMax));
            if (tieneHumMin)   st.setDouble(idx++, Double.parseDouble(humMin));
            if (tieneHumMax)   st.setDouble(idx++, Double.parseDouble(humMax));
            if (tieneRuidoMin) st.setDouble(idx++, Double.parseDouble(ruidoMin));
            if (tieneRuidoMax) st.setDouble(idx++, Double.parseDouble(ruidoMax));
            if (tieneUvMin)    st.setDouble(idx++, Double.parseDouble(uvMin));
            if (tieneUvMax)    st.setDouble(idx++, Double.parseDouble(uvMax));
            if (tieneAireMin)  st.setDouble(idx++, Double.parseDouble(aireMin));
            if (tieneAireMax)  st.setDouble(idx++, Double.parseDouble(aireMax));

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    JsonObject obj = new JsonObject();

                    obj.addProperty("id", rs.getInt("id"));

                    Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) {
                        obj.addProperty("timestamp", ts.toInstant().toString());
                    }

                    obj.addProperty("temperatura", rs.getDouble("temperatura"));
                    obj.addProperty("humedad", rs.getDouble("humedad"));
                    obj.addProperty("radiacion_uv", rs.getDouble("radiacion_uv"));
                    obj.addProperty("ruido_db", rs.getDouble("ruido_db"));
                    obj.addProperty("calidad_aire", rs.getDouble("calidad_aire"));

                    resultados.add(obj);
                }
            }

            resp.getWriter().write(resultados.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Error al consultar mediciones");
            resp.getWriter().write(error.toString());
        }
    }
}
