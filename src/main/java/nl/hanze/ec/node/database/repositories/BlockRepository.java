package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.modules.annotations.BlockDAO;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.sql.SQLException;
import java.util.List;

public class BlockRepository {
    private final Dao<Block, String> blockDAO;
    private String rootMerkleHash;

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

    public Block createBlock(String hash, String previousBlockHash, String merkleRootHash, int blockHeight) {
        Block block = null;
        try {
            block = new Block(hash, previousBlockHash, merkleRootHash, blockHeight);
            blockDAO.createOrUpdate(block);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }

    public String getCurrentBlockHash(int blockHeight) {
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

    public String getRootMerkleHash() {
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

    public int getNumberOfBlocks() {
        int count = 0;
        try {
            count = (int) blockDAO.queryRawValue("select COUNT(block_height) from Blocks");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public int getCurrentBlockHeight() {
        int height = 0;
        try {
            height = (int) blockDAO.queryRawValue("select MAX(block_height) from Blocks");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return height;
    }

    public Integer getBlockHeight(String hash) {
        try {
            List<Block> blocks = blockDAO.queryBuilder()
                    .where().eq("hash", hash).query();

            if (blocks.size() == 1) {
                return blocks.get(0).getBlockHeight();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Block getBlock(int block_height) {
        try {
            List<Block> blocks = blockDAO.queryBuilder()
                    .orderBy("block_height", true)
                    .where().eq("block_height", block_height).query();

            if (blocks.size() == 1) {
                return blocks.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Block getCurrentBlock() {
        Block block = null;
        try {
            block = blockDAO.queryBuilder()
                    .where().eq("block_height", getCurrentBlockHeight()).query().get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return block;
    }
}
