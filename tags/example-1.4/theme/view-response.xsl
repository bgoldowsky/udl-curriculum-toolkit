<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet[
<!ENTITY catts "@id|@class|@title|@xml:lang">
<!ENTITY cncatts "@id|@title|@xml:lang">
 ]>
<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/1999/xhtml" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:wicket="http://wicket.apache.org"
    exclude-result-prefixes="dtb">
    
    <xsl:import href="dtbook2xhtml.xsl"/>
    <xsl:import href="common.xsl"/>    

  <!-- response groups -->
   <xsl:template match="dtb:responsegroup">
     <div class="nohlpassage">
       <xsl:call-template name="responseArea"/>
     </div>
   </xsl:template>
	
	<xsl:template name="responseArea">
   	<xsl:choose>
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
				<form wicket:id="select1_view_immediate"
					rgid="{ancestor-or-self::dtb:responsegroup/@id}"
					title="{ancestor-or-self::dtb:responsegroup/@title}"
					group="{ancestor-or-self::dtb:responsegroup/@group}"
					class="subactivity">					
					<xsl:apply-templates select="dtb:select1" />
				</form>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="responseArea-delay-feedback">
		<xsl:choose>
			<xsl:when test="dtb:select1">
				<form wicket:id="select1_view_delay"
					rgid="{ancestor-or-self::dtb:responsegroup/@id}"
					title="{ancestor-or-self::dtb:responsegroup/@title}"
					group="{ancestor-or-self::dtb:responsegroup/@group}"
					class="subactivity">					
					<xsl:apply-templates select="dtb:select1" />
				</form>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

		
	
</xsl:stylesheet>


   