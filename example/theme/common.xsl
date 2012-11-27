<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet[
<!ENTITY catts "@id|@class|@title|@xml:lang">
<!ENTITY cncatts "@id|@title|@xml:lang">
 ]>
<xsl:stylesheet
    version="1.0"
    xmlns:out="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML"
    xmlns:wicket="http://wicket.apache.org"
    xmlns="http://www.w3.org/1999/xhtml" 
    exclude-result-prefixes="dtb">

	<xsl:param name="lock-response" select="false()"/>    
	<xsl:param name="delay-feedback" select="false()"/> 
	
	<!--  which part of the video thumb should be viewed -->    
	<xsl:param name="add-video-thumb-link" select="true()"/>  

    <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    
    <!-- Set up key on every annotation to the content it references - the first non annotation sibling -->
    <xsl:key name="annokey" match="dtb:annotation" use="following-sibling::*[local-name() != 'annotation'][1]/@id"/>

    <!-- prodnotes are pulled out as "d" links -->
    <xsl:template match="dtb:prodnote"/>

    <!-- pagenum naver displayed -->
    <xsl:template match="dtb:pagenum"/>
    
    <!-- For some reason span isn't handled in the standard dtbook2xhtml -->
    <xsl:template match="dtb:span">
      <span>
        <xsl:copy-of select="&catts;"/>
        <xsl:apply-templates/>
      </span>
    </xsl:template>

   	<xsl:template match="dtb:list[@type='ol']">
       <xsl:variable name="listtype">
           <xsl:choose>
               <xsl:when test="@enum='1'">decimal</xsl:when>
               <xsl:when test="@enum='i'">lower-roman</xsl:when>
               <xsl:when test="@enum='I'">upper-roman</xsl:when>
               <xsl:when test="@enum='a'">lower-alpha</xsl:when>
               <xsl:when test="@enum='A'">upper-alpha</xsl:when>
               <xsl:otherwise></xsl:otherwise>
           </xsl:choose>
       </xsl:variable>
       <ol>
           <xsl:copy-of select="&catts;"/>
           <xsl:copy-of select="@start" />
           <xsl:if test="$listtype != ''">
               <xsl:attribute name="style">
                   <xsl:value-of select="concat('list-style-type: ', $listtype, ';')"/>
               </xsl:attribute>
           </xsl:if>
           <xsl:apply-templates/>
       </ol>
   	</xsl:template>
   	
   	<xsl:template match="dtb:table">
		<table class="dataTable {@class}">
			<xsl:copy-of select="&cncatts;"/>
			<xsl:apply-templates/>
		</table>
   </xsl:template>

    <xsl:template match="dtb:p">
        <p class="hlpassage {@class}">
            <xsl:copy-of select="&cncatts;"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

	<xsl:template match="dtb:list/dtb:li">
		<li class="hlpassage">
			<xsl:copy-of select="&cncatts;"/>
			<xsl:apply-templates />
		</li>
	</xsl:template>

    <!-- Ignore except when called out by image. -->
    <xsl:template match="dtb:imggroup/dtb:caption"/>
    
    <xsl:template match="dtb:caption" mode="caption">
     <div class="imgCaption">
	       <xsl:copy-of select="&catts;"/>
	       <xsl:apply-templates/>
     </div>
   	</xsl:template>

    <xsl:template match="dtb:caption" mode="mediaCaption">
       <xsl:copy-of select="&catts;"/>
       <xsl:apply-templates/>
   	</xsl:template>
    
    <!-- TOGGLE BUTTONS -->
    <xsl:template match="dtb:div[@class='supplement']">
		<div wicket:id= "collapseBox_" class="supportBox collapseBox supplement">
			<h4 wicket:id="collapseBoxControl-" class="toggleOffset">
				<xsl:value-of select="@title" />
			</h4>
			<div wicket:id="feedbackStatusIndicator_">
			</div>
			<div class="collapseBody">
				<xsl:apply-templates />
			</div>
		</div>
	</xsl:template>

    <!-- Slide Show -->
    <xsl:template match="dtb:div[@class='slideshow']">
    	<xsl:variable name="tempId" select='count(preceding::dtb:div[@class="slideshow"])' />    
		<div wicket:id="slideShow_{$tempId}" class="slideshow" id="slideShow_{$tempId}">
			<div class="slideshowTitle">
				<xsl:value-of select="@title" />
			</div>
			<ul>
	         	<xsl:apply-templates select="dtb:div" mode="slideshowLink" />
			</ul>
			<xsl:apply-templates select="dtb:div" mode="slide" />
		</div>
	</xsl:template>

	<!-- put a unique id on the slide div  -->
    <xsl:template match="dtb:div" mode="slide">
    	<xsl:variable name="tempId" select='count(preceding::dtb:div[@class="slide"])' /> 
    	<div id="slide_{$tempId}">
        	<xsl:apply-templates/>
    	</div>
    </xsl:template>

	<!-- these turn into buttons for the slide show with the bridgeheads used as titles -->
    <xsl:template match="dtb:div" mode="slideshowLink">
    	<xsl:variable name="tempId" select='count(preceding::dtb:div[@class="slide"])' /> 
    	<li>
    		<a href="#slide_{$tempId}" title="{dtb:bridgehead/@title}"><xsl:value-of select="dtb:bridgehead" /></a>
    	</li>
   		<xsl:text> </xsl:text>
    </xsl:template>

	<!--  ignore all bridgheads inside the slideshow -->
    <xsl:template match="dtb:div[@class='slideshow']//dtb:bridgehead">
    </xsl:template>
    
    
    <!-- GLOSSARY - link used is determined by application parameter isi.glossary.type -->
    <xsl:template match="dtb:gl">
    	<!--  this is for inline glossary terms -->
  		<a wicket:id="glossword" onclick="return togglespan(this);" class="glossary" href="#">
        	<xsl:apply-templates/>
   		</a>
   		<span class="inlineGlossary" style="display:none">
		   		<xsl:text> </xsl:text>
	   			<span wicket:id="glossdef" word="{@entryId}">definition will be inserted here</span>
  				<xsl:text> </xsl:text>
   				<a wicket:id="glosslink" word="{@entryId}">(more)</a>
  		</span>

    	<!--  this is for glossary links to modal popups -->
  		<a wicket:id="glossaryLink_" class="vocabulary" word="{@entryId}">
	    	<xsl:apply-templates/>
  		</a>
  		
    	<!--  this is for glossary links to main glossary page-->
  		<a wicket:id="glossaryMainLink_" class="vocabulary" word="{@entryId}">
	    	<xsl:apply-templates/>
  		</a>  		
	</xsl:template>


    <!-- VIDEOS, AUDIO -->
    <xsl:template match="dtb:object">

		<!--  determine if the video modal should be added, object must have class = thumb -->
   		<xsl:variable name="thumbVideo">
         	<xsl:choose>
        		<xsl:when test="contains(@class, 'thumb')">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>

      	<xsl:choose>
	       <!-- if there is no source then there is an error -->
	       <xsl:when test="@src=''">
	        	<div style="border:5px inset red">Content error: Object with no src attribute set</div>
	       </xsl:when>
	       
	       <!-- process external youtube videos -->
	       <xsl:when test="contains(@src, 'youtube.com') or contains(@src, 'youtu.be')">
			  	<xsl:if test="$thumbVideo = 'false'">
					<xsl:call-template name="youtube_videotag" />
		       	</xsl:if>
			  	<xsl:if test="$thumbVideo = 'true'">
			  	   	<xsl:choose>
				   		<xsl:when test="boolean($add-video-thumb-link)">
						  	<xsl:call-template name="videoThumbLink" />
				   		</xsl:when>
	   					<xsl:otherwise>
							<xsl:call-template name="videotag" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
	       </xsl:when>
	      
	       <!-- process internal videos -->
	 	   <xsl:when test="contains(@src, '.flv') or contains(@src, '.mp4') or contains(@src, '.mp3')">
			  	<xsl:if test="$thumbVideo = 'false'">
						<xsl:call-template name="videotag" />
		       	</xsl:if>
			  	<xsl:if test="$thumbVideo = 'true'">
			  	   	<xsl:choose>
				   		<xsl:when test="boolean($add-video-thumb-link)">
						  	<xsl:call-template name="videoThumbLink" />
				   		</xsl:when>
	   					<xsl:otherwise>
							<xsl:call-template name="videotag" />
						</xsl:otherwise>
					</xsl:choose>
		       	</xsl:if>
		   </xsl:when>
		   
	       <!-- process flash applets -->
		   <xsl:when test="contains(@src, '.swf')">
		     <div wicket:id="swf_" src="{@src}" width="100" height="100"></div>
		   </xsl:when>
	                   
	 	   <!-- unknown object type -->
	 	   <xsl:otherwise>
	         <div wicket:id="object_" src="{@src}"  width="{@width}" height="{@height}" id="{@id}">
	           <xsl:apply-templates/>
	         </div>
			 <xsl:call-template name="objectCaption" />
	       </xsl:otherwise>
	   </xsl:choose>
    </xsl:template>
       
    <xsl:template match="dtb:param">
	  	<param>
	  		<xsl:copy-of select="@name|@value"/>
	  	</param>
    </xsl:template>

 	<xsl:template name="videoThumbLink" match="/">
        <xsl:variable name="poster" select="dtb:param[@name='poster']/@value"/>
		<div class="objectBox {@class}" id="media_{@id}">
			<div style="width: 250px; height: 170px;" class="mediaPlaceholder">
				<a href="#" wicket:id="mediaThumbLink_" videoId="{@id}">
					<img wicket:id="mediaThumbImage_" src="{$poster}" width="250" height="170" alt="Video Thumbnail" />
					<span class="playIcon"></span>
				</a>
			</div>
			<div class="objectCaption">
				<xsl:call-template name="objectCaption" />
			</div>
		</div>	
	</xsl:template>

	<xsl:template name="objectCaption">
		<xsl:if test="count(child::dtb:caption) > 0 or count(child::dtb:prodnote) > 0">
			<div class="objectCaption">
		       	<div class="objectText">
		       		<xsl:if test="count(child::dtb:caption) > 0">
			       		<xsl:apply-templates select="child::dtb:caption[1]" mode="mediaCaption"/>
		       		</xsl:if>
					<!-- add the toggle if there is more than one caption or a prodnote - long description -->
					<xsl:if test="count(child::dtb:caption) > 1 or count(child::dtb:prodnote) > 0">
						<div class="collapseBox">
							<h5 wicket:id="objectToggleHeader_" src="{@src}">More Information</h5>
							<div class="collapseBody">
								<xsl:apply-templates
									select="child::dtb:caption[position()&gt;1]"
									mode="caption" />
								<xsl:apply-templates select="child::dtb:prodnote"
									mode="prodnote" />
							</div>
						</div>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
	</xsl:template>  
    
    <xsl:template name="youtube_videotag">
		<xsl:variable name="width">
           <xsl:choose>
             <xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when>
             <xsl:otherwise>640</xsl:otherwise>
           </xsl:choose>
         </xsl:variable>
        <xsl:variable name="height">
           <xsl:choose>
             <xsl:when test="@height"><xsl:value-of select="@height"/></xsl:when>
             <xsl:otherwise>360</xsl:otherwise>
           </xsl:choose>
        </xsl:variable>
        <xsl:variable name="src">
          <xsl:choose>
            <xsl:when test="contains(@src, '/watch?v=')">
              <xsl:value-of select="concat('http://www.youtube-nocookie.com/embed/', substring-after(@src, 'watch?v='))"/>
			</xsl:when>
			<xsl:when test="contains(@src, 'youtu.be/')">
              <xsl:value-of select="concat('http://www.youtube-nocookie.com/embed/', substring-after(@src, 'youtu.be/'))"/>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="@src"/></xsl:otherwise>
		  </xsl:choose>
		</xsl:variable>
		<div class="objectBox center">
			<div class="mediaPlaceholder" style="width:{$width}px; height:{$height}px;">
				<iframe width="{$width}" height="{$height}" src="{$src}" frameborder="0" class="captionSizer">must have content</iframe>
			</div>
			<xsl:call-template name="objectCaption" />
   		</div>	    		
    </xsl:template>

    <xsl:template name="videotag">
        <xsl:variable name="width">
            <xsl:choose>
                <xsl:when test="@width != ''">
                    <xsl:value-of select="@width" />
                </xsl:when>
                <xsl:otherwise>400</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="height">
            <xsl:choose>
				<xsl:when test="@height != ''">
                    <xsl:value-of select="@height" />
                </xsl:when>
                <!-- if there is no height for an mp3 just display the control bar -->
       			<xsl:when test="contains(@src, '.mp3')">
                    <xsl:text>25</xsl:text>
	            </xsl:when>
                <xsl:otherwise>
                    <xsl:text>170</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="poster" select="dtb:param[@name='poster']/@value"/>
		<xsl:variable name="captions" select="dtb:param[@name='captions']/@value"/>
		<xsl:variable name="audiodescription" select="dtb:param[@name='audiodescription']/@value"/>		
        <div class="objectBox center">
			<div class="mediaPlaceholder" style="width:{$width}px; height:{$height}px;">
		        <div wicket:id="videoplayer_{@id}" width="{$width}" height="{$height}" src="{@src}" videoId="{@id}">
		        	<xsl:if test="$poster">
		        		<xsl:attribute name="poster"><xsl:value-of select="$poster"/></xsl:attribute>
		        	</xsl:if>
		        	<xsl:if test="$captions">
		        		<xsl:attribute name="captions"><xsl:value-of select="$captions"/></xsl:attribute>
		        	</xsl:if>
		        	<xsl:if test="$audiodescription">
		        		<xsl:attribute name="audiodescription"><xsl:value-of select="$audiodescription"/></xsl:attribute>
		        	</xsl:if>
		        </div>
	        </div>
		 	<xsl:call-template name="objectCaption" />
        </div>
    </xsl:template>


	<!-- thumb ratings -->
	<xsl:template match="dtb:responsegroup[@class='thumbrating']">
		<span class="thumbRatingDescription" wicket:id="thumbRatingDescription_"></span>
		<span wicket:id="thumbRating_" id="{@id}"></span>
	</xsl:template>


    <!-- IMAGES -->
	<xsl:template match="dtb:imggroup">
      <xsl:choose>
  		<xsl:when test="count(./dtb:img) > 1 and @class!='smartImage'">
          <!-- found more than one image - group them -->
			<div class="imgGroup">
	          <xsl:apply-templates/>
			</div>
		</xsl:when>
        <xsl:when test="@class='right'">
          <!-- image float to the right side with text wrap -->
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:when test="@class='left'">
          <!-- image on the left side with text wrap -->
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:when test="@class='center'">
          <!-- image in the center with no text wrap -->
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:when test="@class='smartImage'">
        	<div wicket:id="smartImage-{@id}" class="smartImage">
        		<xsl:copy-of select="&catts;" />
        		<xsl:call-template name="smartImageProcess" />
        	</div>
        </xsl:when>
        <xsl:when test="@class='annotatedImage'">
			<!-- the annotated image id is the id of the first img in this imggroup, send the src of the image for reference processing -->
	        <div wicket:id="annotatedImage_{@id}" annotatedImageId="{./dtb:img/@id[1]}" annotatedImageSrc="{./dtb:img/@src[1]}">
		    	<xsl:call-template name="annotatedImageProcess" />
	    	    <xsl:apply-templates select="key('annokey', @id)[@class='hotspot']" mode="hotspot"/>
	       	</div>
        </xsl:when>
        <xsl:otherwise>
	        <!-- featured image - floats left with no text wrap -->
	        <br clear="left"/>
	        <div class="imggroup">
		        <xsl:apply-templates/>
	        </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
    
    <xsl:template name="smartImageProcess">
    	<xsl:copy-of select="dtb:img" />
    	<xsl:copy-of select="dtb:caption" />
    </xsl:template>

    <xsl:template name="annotatedImageProcess">
   		<xsl:apply-templates/>   		
    </xsl:template>

   	<xsl:template match="dtb:annotation" mode="hotspot">
		<!-- the annotated image id is the id of the img in the first sibling imggroup  -->
	     <span wicket:id="hotSpot_" style="display:none" annotatedImageId="{../dtb:imggroup/dtb:img/@id[1]}"
	     	id="{@id}" title="{@title}" top="{@top}" left="{@left}" width="{@width}" height="{@height}"
	     	imgSrc="{@imgSrc}" imgClass="{@imgClass}">
		       <xsl:apply-templates/>
	     </span>
   	</xsl:template>

	<xsl:template name="modalImageDetail">
   		<xsl:variable name="addImageToggle">
         	<xsl:choose>
        		<xsl:when test="count(../dtb:caption) > 1 or ../dtb:prodnote">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
   		<div class="modalBody" id="imageDetail_{@id}" style="display:none">
		    <div class="modalHeader" role="banner">
		        <div class="modalTitle"></div>
		        <a href="#" class="modalMove button icon"><img src="/img/icons/move.png" width="16" height="16" alt="Move" title="Move" /></a>
	      		<a href="#" class="modalClose button icon" onclick="showImageDetail('{@id}', false); return false">
	        		<img class="imageDetailButton" src="/img/icons/close.png"></img>
	        	</a>	
	    	</div>
		    <div class="modalMainCol">
		     	<div class="imgBox">
		     		<img wicket:id="image_{@id}" src="{@src}" class="captionSizer">
		       			<xsl:copy-of select="&cncatts;" />
		           		<xsl:copy-of select="@alt" />
		           		<xsl:apply-templates/>
		       		</img>
		       		<div class="imgCaption">
				       	<div class="imgText">
				       		<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][1]" mode="caption"/>
						    <!-- want this toggle when the image hasCaptions (more than one caption or a prodnote) -->
					       	<xsl:if test="$addImageToggle = 'true'">
			                    <div class="collapseBox">
			                        <h5 wicket:id="imgDetailToggleHeader" src="{@src}" imageId="{@id}">More Information</h5>
			                        <div class="collapseBody">
			                        <xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][position()&gt;1]" mode="caption"/>
						       		<xsl:apply-templates select="../dtb:prodnote[@imgref=current()/@id]" mode="prodnote"/>
						       		</div>
						       	</div>
						    </xsl:if>
						</div>
		       		</div>
		        </div>
			</div>
		</div>
	</xsl:template>
	
	<xsl:template match="dtb:img">
		<!--  determine if the image detail toggle should be added, it must have either
			  more than one caption or a long description -->
   		<xsl:variable name="addImageToggle">
         	<xsl:choose>
        		<xsl:when test="count(../dtb:caption) > 1 or ../dtb:prodnote">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
		<!--  determine if the larger image modal should be added, img must have class = thumb -->
   		<xsl:variable name="thumbImage">
         	<xsl:choose>
        		<xsl:when test="@class='thumb'">
	            		<xsl:value-of select="'true'" />
        		</xsl:when>
        		<xsl:otherwise>
	            		<xsl:value-of select="'false'" />
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
		<div class="imgBox" id="image_{@id}">
	    	<xsl:choose>
    	    	<xsl:when test="ancestor::dtb:imggroup and ancestor::dtb:imggroup[@class='right']">
        	    	<xsl:attribute name="class">imgBox right</xsl:attribute>
         		</xsl:when>
    	    	<xsl:when test="ancestor::dtb:imggroup and ancestor::dtb:imggroup[@class='left']">
        	    	<xsl:attribute name="class">imgBox left</xsl:attribute>
         		</xsl:when>
    	    	<xsl:when test="ancestor::dtb:imggroup and ancestor::dtb:imggroup[@class='center']">
        	    	<xsl:attribute name="class">imgBox center</xsl:attribute>
         		</xsl:when>
        	</xsl:choose>
		  	<xsl:if test="$thumbImage = 'false'">
	        	<img wicket:id="image_{@id}" src="{@src}" class="captionSizer">
		       		<xsl:copy-of select="&cncatts;" />
		        	<xsl:copy-of select="@alt" />
		        	<xsl:copy-of select="@height" />
		        	<xsl:copy-of select="@width" />
		        	<xsl:apply-templates/>
		       	</img>
	       	</xsl:if>
		  	<xsl:if test="$thumbImage = 'true'">
	        	<img wicket:id="imageThumb_{@id}" src="{@src}" class="thumb captionSizer">
	            	<xsl:copy-of select="&cncatts;" />
	            	<xsl:copy-of select="@alt" />
	        	</img>
        	</xsl:if>
        	<!-- only add the caption div if there is a caption, prodnote, or thumbnail -->
        	<xsl:if test="count(../dtb:caption) > 0 or count(../dtb:prodnote) > 0 or $thumbImage = 'true'">
		        <div class="imgCaption">
				  	<xsl:if test="$thumbImage = 'true'">
			        	  <div class="imgActions">
				            <span wicket:id="imageDetailButton_{@id}" target="{@src}"></span>
				         </div>
				    </xsl:if>
			       	<div class="imgText">
			       		<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][1]" mode="caption"/>
					    <!-- want this toggle when the image hasCaptions (more than one caption or a prodnote) -->
				       	<xsl:if test="$addImageToggle = 'true'">
		                    <div class="collapseBox">
				                <h5 wicket:id="imgToggleHeader" src="{@src}" imageId="{@id}">More Information</h5>
		                        <div class="collapseBody">
		                        <xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][position()&gt;1]" mode="caption"/>
					       		<xsl:apply-templates select="../dtb:prodnote[@imgref=current()/@id]" mode="prodnote"/>
					       		</div>
					       	</div>
					    </xsl:if>
					</div>
				</div>
			</xsl:if>
	    </div>
	    <!-- add the hidden modal with the larger image -->
	  	<xsl:if test="$thumbImage = 'true'">
	  		<xsl:call-template name="modalImageDetail"/>
	  	</xsl:if>	
	</xsl:template>
    
	<!--  prodnotes inside of img groups are long descriptions -->
    <xsl:template match="dtb:prodnote" mode="prodnote">
     <div class="longDescription">
       <xsl:copy-of select="&catts;"/>
       <xsl:apply-templates/>
     </div>
   </xsl:template>

  <!-- response groups -->
   <xsl:template match="dtb:responsegroup">
   	 <xsl:variable name="type">
		<xsl:choose>
			<xsl:when test="dtb:select1">select1</xsl:when>
			<xsl:otherwise>responsearea</xsl:otherwise>
		</xsl:choose>
	 </xsl:variable>
   	 <xsl:variable name="responseClass">
         <xsl:choose>
        	<xsl:when test="@class='compact'">
	            <xsl:value-of select="'responseMCCompact'" />
        	</xsl:when>
 	     </xsl:choose>
  	</xsl:variable>
   	 <xsl:variable name="noAnswer">
         <xsl:choose>
	    	<xsl:when test="count(dtb:select1/dtb:item)>0 and count(dtb:select1/dtb:item[@correct='true'])=0 ">
	    		<xsl:value-of select="true()" />
	    	</xsl:when>
			<xsl:otherwise>
	    		<xsl:value-of select="false()" />
			</xsl:otherwise>
 	     </xsl:choose>
  	</xsl:variable>	 
     <div class="entryBox nohlpassage {$responseClass}">
     	<div class="teacherBar" wicket:id="teacherBar_">
     		<div class="teacherBarLeft">
     	    </div>
      	    <div class="teacherBarRight">
				<xsl:apply-templates select="key('annokey', @id)[@class='teacheronly']" mode="teacheronly" />
        		<a wicket:id="compareResponses_" href="#" class="button" rgid="{ancestor-or-self::dtb:responsegroup/@id}" group="{ancestor-or-self::dtb:responsegroup/@group}" type="{$type}">Compare Responses</a>
            	<xsl:choose>
            		<!-- feedback not allowed for survey type questions -->
            		<xsl:when test="($noAnswer != 'true')">
 			           	<span wicket:id="feedbackButton_" for="teacher" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
 			        </xsl:when>
 			    </xsl:choose>
            	<xsl:choose>
            		<xsl:when test="not($type='select1')">
						<span wicket:id="scoreButtons_" for="teacher"  rgid="{ancestor-or-self::dtb:responsegroup/@id}"  group="{ancestor-or-self::dtb:responsegroup/@group}" type="{$type}"></span>
            		</xsl:when>
            	</xsl:choose>
       	 	</div>
       </div>
       <xsl:apply-templates select="dtb:prompt"/>
       <xsl:call-template name="responseArea"/>
     </div>
   </xsl:template>

   <xsl:template name="responseArea">
	   	<xsl:choose>	   	
	   		<!-- test for single select with no correct answer - no feedback -->
	   		<xsl:when test="count(child::dtb:select1/dtb:item) > 0 and count(child::dtb:select1/dtb:item[@correct='true']) = 0">
	   			<xsl:call-template name="responseArea-delay-feedback" />
	   		</xsl:when>
	   		<xsl:when test="boolean($delay-feedback)">
	   			<xsl:call-template name="responseArea-delay-feedback" />
	   		</xsl:when>
	   		<xsl:otherwise>
	   			<xsl:call-template name="responseArea-immediate-feedback" />
	   		</xsl:otherwise>
	   	</xsl:choose>
	</xsl:template>

	<xsl:template name="responseArea-immediate-feedback">
		<xsl:choose>
			<xsl:when test="dtb:select1">
				<div wicket:id="responseContainer">
					<form wicket:id="select1_immediate_"
						rgid="{ancestor-or-self::dtb:responsegroup/@id}"
						title="{ancestor-or-self::dtb:responsegroup/@title}"
						group="{ancestor-or-self::dtb:responsegroup/@group}"
						class="subactivity">					
						<div wicket:id="shy" class="responseBar">
							<div class="responseLeft"><!-- empty --></div>
							<div class="responseRight">
								<!-- helper links -->
								<xsl:apply-templates select="key('annokey', @id)" mode="showannotations" />
								<span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
								<span wicket:id="mcScore"></span>
							</div>
						</div>
						<xsl:apply-templates select="dtb:select1" />
					</form>
					<div wicket:id="viewActions"
						rgid="{ancestor-or-self::dtb:responsegroup/@id}"
						title="{ancestor-or-self::dtb:responsegroup/@title}"
						group="{ancestor-or-self::dtb:responsegroup/@group}">
					</div>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="responseBar">
					<div wicket:id="responseButtons_" rgid="{@id}" class="responseLeft">
					</div>
					<div class="responseRight">
						<!-- helper links -->
						<xsl:apply-templates select="key('annokey', @id)" mode="showannotations" />
						<span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
						<span wicket:id="showScore_" rgid="{@id}" group="{@group}" type="responsearea"></span>
					</div>
				</div>
				<!-- list of responses -->
				<div wicket:id="responseList_" rgid="{@id}" group="{@group}">
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="responseArea-delay-feedback">
		<xsl:choose>
			<xsl:when test="dtb:select1">
		    	<xsl:variable name="noAnswer" select="boolean(count(ancestor-or-self::dtb:responsegroup/dtb:select1/dtb:item)>0 
 		    											and (count(ancestor-or-self::dtb:responsegroup/dtb:select1/dtb:item[@correct='true'])=0)) "/>     
		    	<xsl:variable name="compact" select="boolean(count(ancestor-or-self::dtb:responsegroup[@class='compact'])>0) "/>     
				<div wicket:id="responseContainer">
					<form wicket:id="select1_delay_"
						rgid="{ancestor-or-self::dtb:responsegroup/@id}"
						title="{ancestor-or-self::dtb:responsegroup/@title}"
						group="{ancestor-or-self::dtb:responsegroup/@group}"
						noAnswer="{$noAnswer}"
						compact="{$compact}"
						class="subactivity">	
						<xsl:if test="$noAnswer != 'true'">
							<div wicket:id="shy" class="responseBar">
								<div class="responseLeft"><!-- empty --></div>
								<div class="responseRight">
									<!-- helper links -->
									<xsl:apply-templates select="key('annokey', @id)" mode="showannotations" />
									<span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
									<span wicket:id="mcScore"></span>
								</div>
							</div>
						</xsl:if>
						<xsl:apply-templates select="dtb:select1" />
					</form>
					<xsl:if test="$compact != 'true'">
						<!-- don't display if this is a compact response -->
						<div wicket:id="viewActions"
							rgid="{ancestor-or-self::dtb:responsegroup/@id}"
							title="{ancestor-or-self::dtb:responsegroup/@title}"
							group="{ancestor-or-self::dtb:responsegroup/@group}"
							behavior="{ancestor-or-self::dtb:responsegroup/@behavior}" >
						</div>
					</xsl:if>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="responseBar">
					<div wicket:id="locking_responseButtons_" rgid="{@id}" class="responseLeft">
					</div>
					<div class="responseRight">
						<!-- helper links -->
						<xsl:apply-templates select="key('annokey', @id)" mode="showannotations" />
						<span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
						<span wicket:id="showScore_" rgid="{@id}" group="{@group}" type="responsearea"></span>
					</div>
				</div>
				<!-- list of responses -->
				<div wicket:id="locking_responseList_" rgid="{@id}" group="{@group}">
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Multiple Choice -->
	<xsl:template match="dtb:select1">	
		<xsl:choose>
	   		<xsl:when test="count(dtb:item) > 0 and count(dtb:item[@correct='true']) = 0">
				<xsl:call-template name="select1-delay-feedback" />
			</xsl:when>
			<xsl:when test="boolean($delay-feedback)">
				<xsl:call-template name="select1-delay-feedback" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="select1-immediate-feedback" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="select1-immediate-feedback">
		<div class="responseItem" wicket:id="radioGroup">
        	<div class="responseMCItem">
				<xsl:apply-templates select="dtb:item//dtb:label" />
			</div>
			<p class="responseMCActions">
				<a href="#" wicket:id="submitLink" class="button">
					Check My Answer
				</a>	
			</p>
			<div class="responseMCFeedback">
				<xsl:apply-templates select="dtb:item//dtb:message" />
				<wicket:enclosure child="selectNone">
					<div class="stResult incorrect">
						<p wicket:id="selectNone" >Select an answer...</p>
					</div>
				</wicket:enclosure>
			</div>
		</div>
	</xsl:template>

	
	<xsl:template name="select1-delay-feedback">
		<xsl:variable name="noAnswer" select="boolean(count(ancestor-or-self::dtb:responsegroup/dtb:select1/dtb:item)>0 
 		    								and (count(ancestor-or-self::dtb:responsegroup/dtb:select1/dtb:item[@correct='true'])=0)) "/>  
		<div class="responseItem" wicket:id="radioGroup">
			<xsl:for-each select="dtb:item">
	        	<div class="responseMCItem">
					<xsl:apply-templates select="dtb:label" />
		            <xsl:if test="$noAnswer != 'true'">
						<xsl:call-template name="select1-delay-message" />
					</xsl:if>
				</div>
			</xsl:for-each>
			<wicket:enclosure child="date">
				<div class="responseMCFeedback">
						<span class="autosave responseTime" >
							<strong>Last Saved:</strong> <span wicket:id="date" >[Date]</span>
						</span>
				</div>
			</wicket:enclosure>
		</div>
	</xsl:template>

	<xsl:template match="dtb:item//dtb:label">
		<xsl:variable name="itemid" select="concat('selectItem_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<xsl:variable name="labelid" select="concat('selectItemLabel_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<div wicket:id="{$itemid}" class="responseMCItem">
			<xsl:copy-of select="ancestor::dtb:item/@correct" />
			<input wicket:id="radio" type="radio">
   				<xsl:copy-of select="&catts;" />
			</input>
			<label wicket:id="label">
				<xsl:apply-templates />
			</label>
		</div>
	</xsl:template>
	
	<xsl:template name="select1-delay-message">
		<xsl:variable name="messageid" select="concat('selectDelayMessage_', ancestor::dtb:responsegroup/@id, '_', @id)"/>
		<div wicket:id="{$messageid}">
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="@correct='true'">stResult correct responseFeedback</xsl:when>
					<xsl:otherwise>stResult incorrect responseFeedback</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="@correct='true'">
					<img style="padding-right:5px;" src="/img/icons/response_positive.png" title="Correct Answer" alt="Correct Answer" />
				</xsl:when>
				<xsl:otherwise>
					<img style="padding-right:5px;" src="img/icons/response_negative.png" title="Incorrect Answer" alt="Incorrect Answer" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="dtb:message" />
		</div>
	</xsl:template>

    <xsl:template match="dtb:item//dtb:message">
		<xsl:variable name="itemid" select="concat('selectItem_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<xsl:variable name="messageid" select="concat('selectMessage_', ancestor::dtb:responsegroup/@id, '_', ancestor::dtb:item/@id)"/>
		<div wicket:id="{$messageid}" for="{$itemid}">
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="ancestor::dtb:item/@correct='true'">stResult correct</xsl:when>
					<xsl:otherwise>stResult incorrect</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:apply-templates />
		</div>
    </xsl:template>

   <!-- ratings  -->
	<xsl:template match="dtb:responsegroup[@class='rating']">
		<div wicket:id="ratePanel_" id="{@id}" type="{@type}">
            <xsl:apply-templates select="dtb:prompt" />
		</div>
	</xsl:template>
      
   <xsl:template match="dtb:prompt">
     <div class="prompt" id="prompt_{ancestor::dtb:responsegroup/@id}">
       <xsl:apply-templates/>
     </div>
   </xsl:template>
   
   <!-- MathML  -->
    <xsl:template match="m:math">
      <!-- The non-standard, ignore="true" attribute tells TextHelp toolbar not to mark up inside the span.
           The class "nohlpassage" tells the highlighting code not to add its spans.
           Any added spans mess up the math display. -->
      <span class="nohlpassage">
        <math>
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates/>
        </math>
      </span>
    </xsl:template>
    
    <xsl:template match="m:*">
      <!-- MathJax does not want the elements to be namespaced. -->
      <xsl:element name="{local-name()}">
        <xsl:copy-of select="@*"/>
      	<xsl:apply-templates/>
      </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>