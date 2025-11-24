package org.coordinate.bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.coordinate.entity.Result;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Named("ResultBean")
@ApplicationScoped
public class ResultBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EntityManagerFactory emf;
    private List<ResultDTO> results;

    @PostConstruct
    public void init() {
        try {
            emf = Persistence.createEntityManagerFactory("CoordinatePU");
            loadResults();
        } catch (Exception e) {
            e.printStackTrace();
            results = new ArrayList<>();
        }
    }

    @PreDestroy
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    /**
     * Load all results from database
     */
    public void loadResults() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            TypedQuery<Result> query = em.createQuery(
                    "SELECT r FROM Result r ORDER BY r.checkTime DESC", Result.class);
            List<Result> entities = query.getResultList();

            results = new ArrayList<>();
            for (Result r : entities) {
                results.add(new ResultDTO(r));
            }
        } catch (Exception e) {
            e.printStackTrace();
            results = new ArrayList<>();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Add new check results
     */
    public void addResult(double x, double y, double r, boolean hit) {
        long startTime = System.nanoTime();
        long executionTime = System.nanoTime() - startTime;

        Result newResult = new Result(x, y, r, hit, LocalDateTime.now(), 0L);
        ResultDTO dto = new ResultDTO(newResult);
        results.add(0, dto);

        Result result = new Result(x, y, r, hit, LocalDateTime.now(), executionTime);

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(result);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }

        loadResults();
    }

    /**
     * Clear all results - JSF action method
     */
    public String clear() {
        clearResults();
        return null; // Stay on the same page
    }

    /**
     * Clear all the results from database
     */
    private void clearResults() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE check_results RESTART IDENTITY").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        loadResults();
    }

    public List<ResultDTO> getResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        return results;
    }

    public void setResults(List<ResultDTO> results) {
        this.results = results;
    }

    /**
     * DTO class for displaying results in XHTML
     */
    public static class ResultDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private Double x;
        private Double y;
        private Double r;
        private Boolean hit;
        private String timestamp;

        public ResultDTO(Result result) {
            this.x = result.getX();
            this.y = result.getY();
            this.r = result.getR();
            this.hit = result.getHit();
            this.timestamp = result.getCheckTime().format(FORMATTER);
        }

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }

        public Double getR() {
            return r;
        }
        public Boolean getHit() {
            return hit;
        }
        public String getTimestamp() {
            return timestamp;
        }

        public void setX(Double x) {
            this.x = x;
        }
        public void setY(Double y) {
            this.y = y;
        }
        public void setR(Double r) {
            this.r = r;
        }
        public void setHit(Boolean hit) {
            this.hit = hit;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}