/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mui.Interface;
import mui.Widget;
import miralib.data.DataTree;
import miralib.data.Variable;
import java.util.ArrayList;

/**
 * Vertical scroller for group, tables, and variables. It updates dynamically 
 * depending on the visible items.
 *
 */

public class RowScroller extends MiraWidget {
  protected DataTree tree;
  protected int current;
  protected float heightOpen;
  protected float heightClose;
  protected SingleScroller groupScroller, tableScroller, varScroller;
  
  public RowScroller(Interface intf, float x, float y, float w, float h, 
                     float openh, float closeh) {
    super(intf, x, y, w, h);
    heightOpen = openh;
    heightClose = closeh;
    tree = data.getTree();
    initItems();
  }
  
  protected void handleResize(int newWidth, int newHeight) {
    bounds.h.set(newHeight - mira.labelHeightClose);
  } 
  
  public boolean plotsReady() {
    return varScroller.plotsReady();
  }
  
  public void dragColumns(float dx) {
    varScroller.dragColumns(dx);    
  }
  
//  public void snapColumns() {
//    varScroller.snapColumns();
//  }

  public void dataChanged() {
    varScroller.dataChanged();
  } 
  
  public void pvalueChanged() {
    varScroller.pvalueChanged();
  }
  
  public void showVariables() {
    while (next(false) != varScroller);    
  }
  
  public void openRow(Variable var) {
    int idx = data.getVariableIndex(var);
    if (-1 < idx) {
      while (next(false) != varScroller);
      varScroller.jumpTo(idx);      
    }
  }

  public ArrayList<RowVariable> getRowVariables() {
    ArrayList<RowVariable> list = new ArrayList<RowVariable>();
    for (int i = 0; i < varScroller.getChildrenCount(); i++) {
      RowVariable rvar = (RowVariable)varScroller.getChild(i);
      list.add(rvar);
    }
    return list;
  }

  public RowPlots getPlots(Variable rvar) {
    return null;
  }

  public void closeRowsBut(MiraWidget wt) {
    varScroller.closeAllBut(wt);
  }
  
  public void closeColumn(Variable var) {
    varScroller.closeColumn(var);
  }
  
  public String getRowLabel(Variable varx, Variable vary) {
    return varScroller.getRowLabel(varx, vary);
  }

  public String getColLabel(Variable varx, Variable vary) {
    return varScroller.getColLabel(varx, vary);
  }  
  
  protected SingleScroller prev() {
    return prev(true);
  }
  
  protected SingleScroller prev(boolean dragCol) {
    if (dragCol && 0 < mira.browser.getFirstColumn()) {
      mira.browser.dragColumns(-mira.plotWidth);
      return (SingleScroller)children.get(current);
    }
    
    if (0 < current) {
      SingleScroller scroller0 = (SingleScroller)children.get(current);
      SingleScroller scroller1 = (SingleScroller)children.get(current - 1);
      scroller0.setActive(false);        
      scroller1.setActive(true, false);
      intf.selectWidget(scroller1);
      if (scroller1 == varScroller) showColumnLabels();
      else hideColumnLabels();
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x + width);
      }
      current--;
      return scroller1;
    } else {      
      return (SingleScroller)children.get(0);
    }    
  } 
  
  protected SingleScroller next(int i) {
    if (current < children.size() - 1) {
      SingleScroller scroller0 = (SingleScroller)children.get(current);
      SingleScroller scroller1 = (SingleScroller)children.get(current + 1);
      scroller0.setActive(false, false);
      scroller1.setActive(true);
      intf.selectWidget(scroller1);
      if (scroller1 == varScroller) showColumnLabels();
      else hideColumnLabels();      
      int idx = scroller0.items.get(i).getFirstChild();
      scroller1.jumpTo(idx);
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x - width);
      }
      current++;
      return scroller1;
    } else {            
      return (SingleScroller)children.get(children.size() - 1);
    }
  }
  
  protected SingleScroller next() {
    return next(true);
  } 
  
  protected SingleScroller next(boolean dragCol) {
    if (current < children.size() - 1) {
      SingleScroller scroller0 = (SingleScroller)children.get(current);
      SingleScroller scroller1 = (SingleScroller)children.get(current + 1);
      scroller0.setActive(false, false); 
      scroller1.setActive(true);
      intf.selectWidget(scroller1);
      if (scroller1 == varScroller) showColumnLabels();
      else hideColumnLabels();      
      for (Widget wt: children) {
        float x = wt.targetX();    
        wt.targetX(x - width);
      }
      current++;
      return scroller1;
    } else {
      if (dragCol) mira.browser.dragColumns(mira.plotWidth);
      return (SingleScroller)children.get(children.size() - 1);
    }
  }  
  
  public void keyPressed() {
    // TODO: keys are hanging the UI, including variable search.
    SingleScroller currScroller = (SingleScroller)children.get(current);
    if (key == CODED) {
      if (keyCode == LEFT) {
        prev(mouseX > right());
      } else if (keyCode == RIGHT) {
        next();
      } else if (keyCode == UP) {
        currScroller.jumpToPrev();
      } else if (keyCode == DOWN) {
        currScroller.jumpToNext();
      }
    } else if (key == ENTER || key == RETURN) {
      currScroller.enter();      
    }
  }
  
  protected void initItems() {
    if (1 < tree.groups.size()) {
      // Initializing all three scroll levels (group, table, variable)
      groupScroller = new SingleScroller(intf, this, 0, 0, width, height);
      groupScroller.setItems(tree.groups, heightOpen, heightClose);
      addChild(groupScroller);
      intf.selectWidget(groupScroller);
      groupScroller.setActive(true);

      tableScroller = new SingleScroller(intf, this, width, 0, width, height);
      tableScroller.setItems(tree.tables, heightOpen, heightClose);
      addChild(tableScroller);
      tableScroller.setActive(false);
      
      varScroller = new SingleScroller(intf, this, 2 * width, 0, width, height);
      varScroller.setItems(tree.variables, heightOpen, heightClose);
      addChild(varScroller);
      varScroller.setActive(false);
    } else if (1 < tree.tables.size()) {
      // Initializing only two scroll levels (table, variable), because there
      // is only one group
      tableScroller = new SingleScroller(intf, this, 0, 0, width, height);
      tableScroller.setItems(tree.tables, heightOpen, heightClose);
      addChild(tableScroller);
      intf.selectWidget(tableScroller);
      tableScroller.setActive(true);
      
      varScroller = new SingleScroller(intf, this, width, 0, width, height);
      varScroller.setItems(tree.variables, heightOpen, heightClose);
      addChild(varScroller);
      varScroller.setActive(false);
    } else {
      // Initializing only one scroll level (variable), because there
      // is only one group and one table. This is the case when no metadata
      // is provided.
      varScroller = new SingleScroller(intf, this, 0, 0, width, height);
      varScroller.setItems(tree.variables, heightOpen, heightClose);
      addChild(varScroller);
      intf.selectWidget(varScroller);
      varScroller.setActive(true);
    }
    current = 0;
  }  
  
  protected void showColumnLabels() {
    mira.browser.showColumnLabels();
  }
  
  protected void hideColumnLabels() {
    mira.browser.hideColumnLabels();
  }
}