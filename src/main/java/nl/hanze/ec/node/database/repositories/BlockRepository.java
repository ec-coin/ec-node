package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.modules.annotations.BlockDAO;

import java.sql.SQLException;
import java.util.List;

public class BlockRepository {
    private final Dao<Block, String> blockDAO;
    private String rootMerkleHash;

    @Inject
    public BlockRepository(@BlockDAO Dao<Block, String> blockDAO) {
        this.blockDAO = blockDAO;
    }

    public synchronized List<Block> getAllBlocks() {
        try {
            return blockDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized Block createBlock(String hash, String previousBlockHash, String merkleRootHash, int blockHeight) {
        Block block = null;
        try {
            block = new Block(hash, previousBlockHash, merkleRootHash, blockHeight);
            blockDAO.createOrUpdate(block);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return block;
    }

    public synchronized void createBlock(Block block) {
        try {
            blockDAO.createOrUpdate(block);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getCurrentBlockHash(int blockHeight) {
        String hash = "";
        try {
            List<Block> block = blockDAO.queryBuilder()
                    .where().eq("block_height", blockHeight).query();
            hash = block.get(0).getHash();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hash;
    }

    public synchronized String getRootMerkleHash() {
        if (rootMerkleHash == null) {
            try {
                List<Block> block = blockDAO.queryBuilder()
                        .where().eq("block_height", 0).query();
                this.rootMerkleHash = block.get(0).getMerkleRootHash();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return rootMerkleHash;
    }

    public synchronized int getCurrentBlockHeight() {
        int height = 0;
        try {
            height = (int) blockDAO.queryRawValue("select MAX(block_height) from Blocks");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return height;
    }

    public synchronized Block getCurrentBlock() {
        Block block = null;
        try {
            block = blockDAO.queryBuilder()
                    .where().eq("block_height", getCurrentBlockHeight()).query().get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }

    public synchronized Block getBlock(String hash) {
        Block block = null;
        try {
            List<Block> query = blockDAO.queryBuilder()
                    .where().eq("hash", hash).query();

            if (query != null && query.size() > 0) {
                block = query.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return block;
    }
}
