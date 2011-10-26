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
           <xsl:if test="$listtype != ''">
               <xsl:attribute name="style">
                   <xsl:value-of select="concat('list-style-type: ', $listtype, ';')"/>
               </xsl:attribute>
           </xsl:if>
           <xsl:apply-templates/>
       </ol>
   </xsl:template>

    <xsl:template match="dtb:p">
        <p class="hlpassage">
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

    
    <xsl:template name="basename">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="contains($path, '/')">
                <xsl:call-template name="basename">
                    <xsl:with-param name="path" select="substring-after($path, '/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$path"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- TOGGLE BUTTONS -->

    <xsl:template match="dtb:div[@class='supplement']">
		<div class="supportBox collapseBox">
			<h4 wicket:id="collapseBoxControl-" class="toggleOffset">
				<xsl:value-of select="@title" />
			</h4>
			<div class="collapseBody">
				<xsl:apply-templates />
			</div>
		</div>
	</xsl:template>

    <!-- GLOSSARY - link used is determined by application parameter isi.glossary.type -->
    <xsl:template match="dtb:gl">
    	<!--  this is for inline glossary terms -->
  		<a wicket:id="glossword" onclick="return togglespan(this);" class="glossary" href="#">
        	<xsl:apply-templates/>
   		</a>
   		<xsl:text> </xsl:text>
   		<span class="inlineGlossary" style="display:none">
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
      <xsl:choose>
       <xsl:when test="@src=''">
        	<div style="border:5px inset red">Content error: Object with no src attribute set</div>
       </xsl:when>
       
       <xsl:when test="contains(@src, 'youtube.com')">
	        <xsl:variable name="width">
	           <xsl:choose>
	             <xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when>
	             <xsl:otherwise>576</xsl:otherwise>
	           </xsl:choose>
	         </xsl:variable>
	        <xsl:variable name="height">
	           <xsl:choose>
	             <xsl:when test="@height"><xsl:value-of select="@height"/></xsl:when>
	             <xsl:otherwise>351</xsl:otherwise>
	           </xsl:choose>
	         </xsl:variable>
			<div class="objectBox">
	        	<div wicket:id="youtube_" src="{@src}" width="{$width}" height="{$height}"></div>
    		 	<xsl:apply-templates select="./dtb:caption|../dtb:caption[@imgref=current()/@id]" mode="caption" />
    		</div>
       </xsl:when>
      
 	   <xsl:when test="contains(@src, '.flv')">
         <!-- embedded movie -->
		 <xsl:call-template name="videotag" />
         <!-- Long Description -->
		 <xsl:if test="//dtb:prodnote[@imgref=current()/@id]">
			<a wicket:id="ld_{@id}" onclick="return popupWin(this);" target="imgdesc"
				href="#" title="Image description">d</a>
		 </xsl:if>
	   </xsl:when>
            
       <xsl:when test="contains(@src, '.mp3')">
         <!-- embedded audio file -->
         <xsl:variable name="width">
           <xsl:choose>
             <xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when>
             <xsl:when test="ancestor::dtb:annotation">200</xsl:when> <!-- default to something that works in sidebar. -->
             <xsl:otherwise>400</xsl:otherwise>
           </xsl:choose>
         </xsl:variable>
         <div wicket:id="audioplayer_{count(preceding::dtb:object)}" src="{@src}" width="{$width}"/>
         <!-- Long Description -->
         <xsl:if test="//dtb:prodnote[@imgref=current()/@id]">
           <a wicket:id="ld_{@id}" onclick="return popupWin(this);" target="imgdesc" href="#" title="Image description">d</a>
       	 </xsl:if>
		 <xsl:apply-templates select="./dtb:caption|../dtb:caption[@imgref=current()/@id]" mode="caption" />
       </xsl:when>
       
 	   <xsl:otherwise>
 	     <!-- unknown object type -->
         <div wicket:id="object_{count(preceding::dtb:object)}" src="{@src}" width="{@width}" height="{@height}">
           <xsl:apply-templates/>
         </div>
		 <xsl:apply-templates select="./dtb:caption|../dtb:caption[@imgref=current()/@id]" mode="caption" />
       </xsl:otherwise>
   	 </xsl:choose>

	 <!-- caption for multimedia element -->
<!--	 <xsl:apply-templates select="./dtb:caption|../dtb:caption[@imgref=current()/@id]" mode="caption" />-->

    </xsl:template>

    <xsl:template name="videotag">
        <xsl:variable name="base">
            <xsl:call-template name="basename">
                <xsl:with-param name="path" select="@src"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="width">
            <xsl:choose>
                <xsl:when test="@width != ''">
                    <xsl:value-of select="@width" />
                </xsl:when>
                <xsl:otherwise>200</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="height">
            <xsl:choose>
				<xsl:when test="@height != ''">
                    <xsl:value-of select="@height" />
                </xsl:when>
                <xsl:otherwise>170</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <div class="objectBox center">
	        <div wicket:id="videoplayer_{@id}" width="{$width}" height="{$height}" src="{$base}"></div>
            <div class="objectCaption">
            	<xsl:apply-templates select="dtb:caption" />
            </div>
        </div>
    </xsl:template>

	<xsl:template match="dtb:responsegroup[@class='thumbrating']">
 		<p>
     		<strong>Rate this:</strong>    
			<span wicket:id="thumbRating_" id="{@id}">
			</span>
		</p>
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
	     	title="{@title}" top="{@top}" left="{@left}" width="{@width}" height="{@height}"
	     	icon="{@icon}" iconImg="{@iconImg}" iconClass="{@iconClass}">
		       <xsl:apply-templates/>
	     </span>
   </xsl:template>


	<xsl:template name="modalImageDetail">
   		<div class="modalBody" id="imageDetail_{@id}" style="display:none">
	     	<div class="imgBox">
	      		<a href="#" class="closeIcon button icon" onclick="showImageDetail('{@id}', false); return false">
	        		<img class="imageDetailButton" src="/img/icons/close.png"></img>
	        	</a>	
	     		<img wicket:id="image_{@id}" src="{@src}" class="captionSizer">
	       			<xsl:copy-of select="&cncatts;" />
	           		<xsl:copy-of select="@alt" />
	           		<xsl:apply-templates/>
	       		</img>
	       		<div class="imgCaption">
	       			<!--  TODO check the order of these -->
		       		<xsl:apply-templates select="../dtb:prodnote[@imgref=current()/@id]" mode="prodnote"/>
		       		<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id]" mode="caption"/>
	       		</div>
	        </div>
		</div>
	</xsl:template>
	
	<xsl:template match="dtb:img">
		<!--  determine if the image detail modal should be added, it must have either
			  more than one caption or a long description -->
   		<xsl:variable name="addImageModal">
         	<xsl:choose>
        		<xsl:when test="count(../dtb:caption) > 1 or ../dtb:prodnote">
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
        	<!-- Only add the image detail if there are more than 1 caption or long descriptions -->
	       	<xsl:if test="$addImageModal = 'true'">
	       			<!-- moreInfo is used to tell the imageDetailButton which icon to choose -->
			        <span wicket:id="imageDetailButton_{@id}" target="{@src}" moreInfo="true">
	            		<xsl:attribute name="hasCaptions">true</xsl:attribute>
   		     		</span>
	       	</xsl:if>
        	<img wicket:id="image_{@id}" src="{@src}" class="captionSizer">
	       		<xsl:copy-of select="&cncatts;" />
	        	<xsl:copy-of select="@alt" />
	        	<xsl:apply-templates/>
	       	</img>
       		<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][1]" mode="caption"/>
	    </div>

	    <!-- want this when the image hasCaptions (more than one caption or a prodnote) -->
       	<xsl:if test="$addImageModal = 'true'">
       		<xsl:call-template name="modalImageDetail"/>
       	</xsl:if>
	</xsl:template>

    <xsl:template match="dtb:img[@class='thumb']">
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
            <span wicket:id="imageDetailButton_{@id}" target="{@src}">
            	<xsl:if test="count(../dtb:caption) > 1 or ../dtb:prodnote">
            		<xsl:attribute name="hasCaptions">true</xsl:attribute>
            	</xsl:if>
         	</span>
        	<img wicket:id="imageThumb_{@id}" src="{@src}" class="thumb captionSizer">
            	<xsl:copy-of select="&cncatts;" />
            	<xsl:copy-of select="@alt" />
        	</img>
         	<xsl:apply-templates select="../dtb:caption[@imgref=current()/@id][1]" mode="caption"/>
      	</div>
   		<xsl:call-template name="modalImageDetail"/>
    </xsl:template>
    
    <xsl:template match="dtb:prodnote" mode="prodnote">
     <div class="longDescription">
       <xsl:copy-of select="&catts;"/>
       <xsl:apply-templates/>
     </div>
   </xsl:template>

   <!-- link to external site -->
   <xsl:template match="dtb:a[@external='true']" priority="1">
     <xsl:variable name="base">
       <xsl:call-template name="basename">
         <xsl:with-param name="path" select="@href"/>
       </xsl:call-template>
     </xsl:variable>
     <a target="_blank">
       <xsl:copy-of select="&catts;"/>
       <xsl:choose>
         <xsl:when test="starts-with(@href,'http:')">
           <xsl:copy-of select="@href"/>
         </xsl:when>
         <xsl:otherwise>
           <xsl:attribute name="href">
             <xsl:value-of select="concat('resources/img/',$base)"/>
           </xsl:attribute>
         </xsl:otherwise>
       </xsl:choose>
       <xsl:apply-templates/>
     </a>
   </xsl:template>
    
   <!-- response groups -->

   <xsl:template match="dtb:responsegroup">
     <div class="entryBox nohlpassage">
     	<div class="teacherBar" wicket:id="teacherBar_">
     		<div class="teacherBarLeft">
     	    </div>
      	    <div class="teacherBarRight">
        		<a wicket:id="compareResponses_" href="#" class="button" rgid="{ancestor-or-self::dtb:responsegroup/@id}">Compare Responses</a>
            	<span wicket:id="feedbackButton_" for="teacher" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
       	 	</div>
       </div>
       <xsl:apply-templates select="dtb:prompt"/>
       <div class="responseBar">
           <div wicket:id="responseButtons_" rgid="{@id}" class="responseLeft">
           </div>
           <div class="responseRight">
             <!-- helper links -->
             <xsl:apply-templates select="key('annokey', @id)" mode="showannotations"/>
             <span wicket:id="feedbackButton_" for="student" rgid="{ancestor-or-self::dtb:responsegroup/@id}"></span>
           </div>
       </div>
 
       <!-- list of responses -->
       <div wicket:id="responseList_" rgid="{@id}" group="{@group}">
       </div>
     </div>
   </xsl:template>

   <!-- ratings  -->
	<xsl:template match="dtb:responsegroup[@class='rating']">
		<div wicket:id="ratePanel_" id="{@id}" type="{@type}">
            <xsl:apply-templates select="dtb:prompt" />
		</div>
	</xsl:template>
      
   <xsl:template match="dtb:prompt">
     <div class="prompt" id="prompt_{ancestor-or-self::dtb:responsegroup/@id}">
       <xsl:apply-templates/>
     </div>
   </xsl:template>
   


    
</xsl:stylesheet>
