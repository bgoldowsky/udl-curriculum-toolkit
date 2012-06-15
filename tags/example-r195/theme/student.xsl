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
    
    <xsl:import href="dtbook2xhtml.xsl"/>
    <xsl:import href="common.xsl"/>

   <!-- link to another page of our content -->
   <xsl:template match="dtb:a[@href]">
     <a wicket:id="link_{count(preceding::dtb:a[@href])}">
       <xsl:copy-of select="&catts;"/>
       <xsl:copy-of select="@href"/>
       <xsl:apply-templates/>
     </a>
   </xsl:template>     

   <!-- link to a file or an external URL -->
   <xsl:template match="dtb:a[@external='true']" priority="1">
       	<xsl:choose>
       		<xsl:when test="starts-with(@href,'http:') or starts-with(@href, 'https:') or starts-with(@href, 'mailto:')">
		    <a target="_blank">
		       	<xsl:copy-of select="&catts;"/>
	           	<xsl:copy-of select="@href"/>
	           	<xsl:apply-templates/>
     		</a>
      		</xsl:when>
      		<xsl:otherwise>
			<a wicket:id="fileLink_">
		       	<xsl:copy-of select="&catts;"/>
	           	<xsl:copy-of select="@href"/>
	           	<xsl:apply-templates/>
	        </a>
           </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
  
   <xsl:template match="dtb:sidebar[@class='warning']">
     <div class="hlpassage safety">
       <xsl:copy-of select="&cncatts;"/>
       <xsl:apply-templates/>
     </div>
   </xsl:template>

   <xsl:template match="dtb:sidebar[@class='warning']/dtb:hd">
     <h3>
       <xsl:apply-templates/>
     </h3>
   </xsl:template>

   <!-- AGENTS -->
   <xsl:template match="dtb:annotation">
     <!-- show annotation in place unless it's followed by a responsegroup in which case it gets moved into there -->
     <!-- also exclude the hotspot class - these are associated with image annotations -->
     <xsl:if test="local-name(following-sibling::*[local-name() != 'annotation'][1]) != 'responsegroup'">
       <div class="floatBtn">
         <xsl:apply-templates select="." mode="showannotations"/>
       </div>
     </xsl:if>
   </xsl:template>

    <!--  these highlight annotations don't show up on the regular page -->
   <xsl:template match="dtb:annotation[@class='highlight']">
   </xsl:template>

    <!--  these hotspot image annotations don't show up on the regular page -->
   <xsl:template match="dtb:annotation[@class='hotspot']">
   </xsl:template>

	<!-- These are annotations or agents that are associated with the response areas -->
   <xsl:template match="dtb:annotation" mode="showannotations">
     <span wicket:id="agent_" responseAreaId="{@id}">
       <xsl:copy-of select="&catts;"/>
       <xsl:apply-templates/>
     </span>
   </xsl:template>

</xsl:stylesheet>