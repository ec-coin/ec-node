package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.modules.annotations.BlockDAO;
import java.sql.SQLException;
import java.util.List;

public class BlockRepository {
    private final Dao<Block, String> blockDAO;

    @Inject
    public BlockRepository(@BlockDAO Dao<Block, String> blockDAO) {
        this.blockDAO = blockDAO;
    }

    public List<Block> getAllBlocks() {
        try {
            return blockDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void createBlock(String hash, String previousBlockHash, String merkleRootHash, int blockHeight) {
        try {
            Block block = new Block(hash, previousBlockHash, merkleRootHash, blockHeight);
            blockDAO.createOrUpdate(block);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
