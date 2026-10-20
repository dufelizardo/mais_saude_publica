package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.valid.IsValidOp;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometria do território (ADR-0125): GeoJSON em WGS84, guardado como texto, sem PostGIS (ADR-0108). Aceita Polygon e
 * MultiPolygon, ou uma Feature (ou FeatureCollection de uma só) com um deles, para o arquivo exportado de outro SIG.
 * Guarda só a geometria. As contas (dentro de, sobreposição) são do JTS, em graus.
 */
final class GeometriaTerritorial {

    /** Folga de borda para "dentro da área", em graus: cerca de 1 m no equador. */
    static final double TOLERANCIA_BORDA = 0.00001;

    /** Sobreposição abaixo disso é ruído de desenho na borda comum, em graus²: cerca de 12 m². */
    static final double SOBREPOSICAO_MINIMA = 0.000000001;

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final GeometryFactory FABRICA = new GeometryFactory(new PrecisionModel(), 4326);

    private GeometriaTerritorial() {
    }

    /** O GeoJSON normalizado (só a geometria) ou nulo; inválido, 400. */
    static String normalizar(String geojson) {
        if (geojson == null || geojson.isBlank()) {
            return null;
        }
        JsonNode geometria = geometria(ler(geojson));
        ler(geometria);
        try {
            return JSON.writeValueAsString(geometria);
        } catch (Exception e) {
            throw new ResourceBadRequestException("Não foi possível guardar a geometria.");
        }
    }

    /** A geometria JTS de um GeoJSON já normalizado; nulo sem geometria. */
    static Geometry jts(String geojson) {
        return geojson == null ? null : ler(geometria(ler(geojson)));
    }

    /** {@code dentro} cabe em {@code fora}, com a folga de borda. */
    static boolean dentro(Geometry dentro, Geometry fora) {
        return fora.buffer(TOLERANCIA_BORDA).covers(dentro);
    }

    /** As duas se sobrepõem além do ruído de borda; encostar não é sobrepor. */
    static boolean sobrepoe(Geometry a, Geometry b) {
        return a.intersects(b) && a.intersection(b).getArea() > SOBREPOSICAO_MINIMA;
    }

    /** O ponto (longitude, latitude) cai na geometria, com a folga de borda (ADR-0127). */
    static boolean contem(Geometry g, double latitude, double longitude) {
        return g.buffer(TOLERANCIA_BORDA).covers(FABRICA.createPoint(new Coordinate(longitude, latitude)));
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    private static JsonNode ler(String texto) {
        try {
            return JSON.readTree(texto);
        } catch (Exception e) {
            throw invalida("o texto não é JSON");
        }
    }

    private static JsonNode geometria(JsonNode no) {
        String tipo = no == null ? null : no.path("type").asText(null);
        if ("FeatureCollection".equals(tipo)) {
            JsonNode features = no.path("features");
            if (!features.isArray() || features.size() != 1) {
                throw invalida("a FeatureCollection precisa ter exatamente uma feature");
            }
            return geometria(features.get(0));
        }
        if ("Feature".equals(tipo)) {
            return geometria(no.path("geometry"));
        }
        if (!"Polygon".equals(tipo) && !"MultiPolygon".equals(tipo)) {
            throw invalida("use Polygon ou MultiPolygon");
        }
        ObjectNode limpo = JSON.createObjectNode();
        limpo.put("type", tipo);
        limpo.set("coordinates", no.path("coordinates"));
        return limpo;
    }

    private static Geometry ler(JsonNode geometria) {
        JsonNode coordenadas = geometria.path("coordinates");
        Geometry g;
        if ("Polygon".equals(geometria.path("type").asText())) {
            g = poligono(coordenadas);
        } else {
            if (!coordenadas.isArray() || coordenadas.isEmpty()) {
                throw invalida("o MultiPolygon não tem polígonos");
            }
            List<Polygon> partes = new ArrayList<>();
            coordenadas.forEach(p -> partes.add(poligono(p)));
            g = FABRICA.createMultiPolygon(partes.toArray(Polygon[]::new));
        }
        IsValidOp validacao = new IsValidOp(g);
        if (!validacao.isValid()) {
            throw invalida(validacao.getValidationError().getMessage().toLowerCase() + " perto de "
                    + validacao.getValidationError().getCoordinate());
        }
        return g;
    }

    private static Polygon poligono(JsonNode aneis) {
        if (!aneis.isArray() || aneis.isEmpty()) {
            throw invalida("o polígono não tem anel");
        }
        LinearRing externo = anel(aneis.get(0));
        LinearRing[] buracos = new LinearRing[aneis.size() - 1];
        for (int i = 1; i < aneis.size(); i++) {
            buracos[i - 1] = anel(aneis.get(i));
        }
        return FABRICA.createPolygon(externo, buracos);
    }

    private static LinearRing anel(JsonNode pontos) {
        if (!(pontos instanceof ArrayNode lista) || lista.size() < 4) {
            throw invalida("cada anel precisa de pelo menos 4 posições, a última igual à primeira");
        }
        Coordinate[] c = new Coordinate[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            JsonNode p = lista.get(i);
            if (!p.isArray() || p.size() < 2 || !p.get(0).isNumber() || !p.get(1).isNumber()) {
                throw invalida("cada posição é [longitude, latitude]");
            }
            double lon = p.get(0).asDouble();
            double lat = p.get(1).asDouble();
            if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
                throw invalida("a posição [" + lon + ", " + lat + "] está fora do WGS84 (longitude entre -180 e 180, latitude entre -90 e 90)");
            }
            c[i] = new Coordinate(lon, lat);
        }
        if (!c[0].equals2D(c[c.length - 1])) {
            throw invalida("o anel não fecha: a última posição precisa ser igual à primeira");
        }
        return FABRICA.createLinearRing(c);
    }

    private static ResourceBadRequestException invalida(String motivo) {
        return new ResourceBadRequestException("Geometria inválida: " + motivo + ".");
    }
}
