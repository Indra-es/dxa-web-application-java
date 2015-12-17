package com.sdl.webapp.tridion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sdl.webapp.common.api.content.StaticContentNotFoundException;
import com.tridion.broker.StorageException;
import com.tridion.storage.BinaryMeta;
import com.tridion.storage.BinaryVariant;
import com.tridion.storage.ItemMeta;
import com.tridion.storage.StorageManagerFactory;
import com.tridion.storage.StorageTypeMapping;
import com.tridion.storage.dao.BinaryVariantDAO;
import com.tridion.storage.dao.ItemDAO;

/**
 * Encapsulates static files Metadata information.
 * 
 */
public class StaticContentMetaInfo {
	
private static final Logger LOG = LoggerFactory.getLogger(StaticContentMetaInfo.class);
	
	private BinaryVariant binaryVariant; 
	private ItemMeta itemMeta;
	
	/**
	 * C-tor
	 * @param publicationId
	 * @param pathInfo
	 * @throws StorageException
	 * @throws StaticContentNotFoundException
	 */
	public StaticContentMetaInfo(int publicationId, StaticContentPathInfo pathInfo) 
			        throws StorageException, StaticContentNotFoundException{
		
		binaryVariant = findBinaryVariant(publicationId, pathInfo);
		if (binaryVariant == null) {
            throw new StaticContentNotFoundException("No binary variant found for: [" 
		                            + publicationId + "] " + pathInfo.getFileName());
        } 
		itemMeta = findItemMeta(pathInfo);
	}
	
	// No Setters (read-only)
	public BinaryVariant getBinaryVariant() {
		return binaryVariant;
	}
	public ItemMeta getItemMeta() {
		return itemMeta;
	}
	
	/**
     * 
     * @return the file's content type
     */
    public String getContentType(){
    	return binaryVariant.getBinaryType();
    }
    
    /**
     * Used to determine if the file needs refreshing
     * @return Last publish time in millis.
     */
    public long getLastPublishTime() {   	
    	return itemMeta.getLastPublishDate().getTime();
    }
    
    /**
     * 
     * @return publication ID
     */
    public int getPublicationId(){
    	return itemMeta.getPublicationId();
    }
    
    /**
     * 
     * @return item ID
     */
    public int getItemId(){
    	return itemMeta.getItemId();
    }
    
    /**
     * 
     * @return variant ID
     */
    public String getVariantId(){
    	return binaryVariant.getVariantId();
    }
    
    /**
     * Get the {@code BinaryVariant} from the database.
     * 
     * @param publicationId
     * @param pathInfo
     * @return BinaryVariant
     * @throws StorageException
     * @throws StaticContentNotFoundException
     */
    private BinaryVariant findBinaryVariant(int publicationId, StaticContentPathInfo pathInfo)
    		                               throws StorageException, StaticContentNotFoundException{
    	
    	LOG.debug("Accessing Tridion Database to get BinaryVariant...");
        final BinaryVariantDAO bvdao = (BinaryVariantDAO) StorageManagerFactory.getDAO(publicationId, 
        		                    		                       StorageTypeMapping.BINARY_VARIANT);   
        // binaryVariant is set here
        return bvdao.findByURL(publicationId, pathInfo.getFileName());
    }
    
	/**
	 * Get the {@code ItemMeta} from the database using the {@code binaryVariant}.
	 * 
	 * @param pathInfo
	 * @return ItemMeta 
	 * @throws StorageException
	 * @throws StaticContentNotFoundException
	 */
    private ItemMeta findItemMeta( StaticContentPathInfo pathInfo) 
    		                     throws StorageException, StaticContentNotFoundException {
              
        final BinaryMeta binaryMeta = binaryVariant.getBinaryMeta();
    	LOG.debug("Accessing Tridion Database to get Metadata...");
    	
        final ItemDAO idao = (ItemDAO) StorageManagerFactory.getDAO(binaryMeta.getPublicationId(), 
        		            		                                StorageTypeMapping.ITEM_META);        
        return idao.findByPrimaryKey(binaryMeta.getPublicationId(), binaryMeta.getItemId());
    }
    

}
