package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.modules.annotations.NeighbourDAO;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.List;

public class NeighboursRepository {
    private final Dao<Neighbour, String> neighbourDAO;

    @Inject
    public NeighboursRepository(@NeighbourDAO Dao<Neighbour, String> neighbourDAO) {
        this.neighbourDAO = neighbourDAO;
    }

    public List<Neighbour> getAllNeighbours() {
        try {
            return neighbourDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateNeighbour(String ip, int port) {
        try {
            ip = InetAddress.getByName(ip).getHostAddress();
            Neighbour neighbour = new Neighbour(ip, port);
            neighbourDAO.createOrUpdate(neighbour);
        } catch (SQLException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
