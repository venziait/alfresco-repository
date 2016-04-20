package org.alfresco.repo.template;

import java.util.List;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.repo.template.BaseContentNode.TemplateContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * @author Kevin Roast
 * 
 * FreeMarker custom method to return the first N characters of a content stream.
 * <p>
 * Usage: String cropContent(TemplateContentData content, int length)
 */
public final class CropContentMethod extends BaseProcessorExtension implements TemplateMethodModelEx
{
	private static final Log logger = LogFactory.getLog(CropContentMethod.class);
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = null;
        
        if (args.size() == 2)
        {
            Object arg0 = args.get(0);
            Object arg1 = args.get(1);
            
            if (arg0 instanceof BeanModel && arg1 instanceof TemplateNumberModel)
            {
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped instanceof TemplateContentData)
                {
                    int bytes = ((TemplateNumberModel)arg1).getAsNumber().intValue();
                    
                    try 
                    {
                	    result = ((TemplateContentData)wrapped).getContentAsText(bytes);
                    } 
                    catch (ContentIOException e)
                    {
                       logger.warn("unable to getContentAsText", e);
                	   /*
                        * Unable to extract content - return empty text instead.
                        * Probably here through a transformation failure.
                        * This method is called from FreeMarker so throwing the 
                        * exception causes problems.
                        */
                       result = "";
                    }
                }
            }
        }
        
        return result != null ? result : "";
    }
}
