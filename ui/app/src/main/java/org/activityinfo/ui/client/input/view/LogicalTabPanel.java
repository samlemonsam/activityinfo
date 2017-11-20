package org.activityinfo.ui.client.input.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.ScrollDirection;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.gestures.GestureRecognizer;
import com.sencha.gxt.core.client.gestures.LongPressOrTapGestureRecognizer;
import com.sencha.gxt.core.client.gestures.TapGestureRecognizer;
import com.sencha.gxt.core.client.gestures.TouchData;
import com.sencha.gxt.core.client.util.AccessStack;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.fx.client.FxElement;
import com.sencha.gxt.fx.client.animation.AfterAnimateEvent;
import com.sencha.gxt.fx.client.animation.AfterAnimateEvent.AfterAnimateHandler;
import com.sencha.gxt.fx.client.animation.Fx;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ComponentHelper;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.HasLayout;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.BeforeCloseEvent;
import com.sencha.gxt.widget.core.client.event.BeforeCloseEvent.BeforeCloseHandler;
import com.sencha.gxt.widget.core.client.event.BeforeCloseEvent.HasBeforeCloseHandlers;
import com.sencha.gxt.widget.core.client.event.CloseEvent;
import com.sencha.gxt.widget.core.client.event.CloseEvent.CloseHandler;
import com.sencha.gxt.widget.core.client.event.CloseEvent.HasCloseHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A clone of {@link TabPanel} that maps tabs to logical models rather than widgets.
 */
public class LogicalTabPanel<M> extends Component implements
        HasBeforeSelectionHandlers<M>, HasSelectionHandlers<M>, HasBeforeCloseHandlers<M>,
        HasCloseHandlers<M>, HasLayout, HasOneWidget {


    protected class DefaultTabPanelMessages implements TabPanel.TabPanelMessages {

        public String closeOtherTabs() {
            return DefaultMessages.getMessages().tabPanelItem_closeOtherText();
        }

        public String closeTab() {
            return DefaultMessages.getMessages().tabPanelItem_closeText();
        }

    }

    private final TabPanel.TabPanelAppearance appearance;

    protected Menu closeContextMenu;
    private GestureRecognizer closeContextMenuGestureRecognizer;
    private boolean animScroll = true;
    private boolean autoSelect = true;
    private boolean bodyBorder = true;
    private boolean closeMenu = false;
    private boolean scheduledDelegateUpdates;
    private TabPanel.TabPanelMessages messages;
    private boolean resizeTabs = false;
    private int scrollDuration = 150;
    private int scrollIncrement = 100;
    private boolean scrolling;
    private AccessStack<M> stack;
    private boolean tabScroll = false;
    private M contextMenuItem;

    private int tabMargin = 2;
    private int tabWidth = 120;
    private int minTabWidth = 30;

    private HashMap<M, TabItemConfig> configMap = new HashMap<>();

    private M activeModel;

    private SimpleContainer container = new SimpleContainer() {
        @Override
        protected Widget getParentLayoutWidget() {
            return container.getParent();
        }
    };

    private List<M> models = new ArrayList<M>();

    /**
     * Creates a new tab panel with the default appearance.
     */
    public LogicalTabPanel() {
        this(GWT.create(TabPanel.TabPanelAppearance.class));
    }

    /**
     * Creates a new tab panel with the specified appearance.
     *
     * @param appearance the appearance of the tab panel
     */
    public LogicalTabPanel(TabPanel.TabPanelAppearance appearance) {
        this.appearance = appearance;

        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appearance.render(sb);

        setElement((Element) XDOM.create(sb.toSafeHtml()));

        ComponentHelper.setParent(this, container);
        appearance.getBody(getElement()).appendChild(container.getElement());

        setDeferHeight(true);

        addGestureRecognizer(new TapGestureRecognizer() {

            @Override
            protected void onTap(TouchData touchData) {
                LogicalTabPanel.this.onTap(touchData.getLastNativeEvent().<Event>cast());
                super.onTap(touchData);
            }

            @Override
            protected void handlePreventDefault(NativeEvent event) {
                XElement target = event.getEventTarget().cast();
                if (getAppearance().getBar(getElement()).isOrHasChild(target)) {
                    event.preventDefault();
                }
            }
        });
    }

    @Override
    public Widget getWidget() {
        return container.getWidget();
    }

    @Override
    public void setWidget(Widget w) {
        container.setWidget(w);
    }

    @Override
    public void setWidget(IsWidget w) {
        container.setWidget(w);
    }


    /**
     * Adds an item to the tab panel with the specified text. Shorthand for {@link #add(M, TabItemConfig)}.
     *
     * @param model the widget to add to the tab panel
     * @param text the text for the tab
     */
    public void add(M model, String text) {
        insert(model, getModelCount(), new TabItemConfig(text));
    }

    /**
     * Adds an item to the tab panel with the specified tab configuration.
     *
     * @param model the item to add to the tab panel
     * @param config the configuration of the tab
     */
    public void add(M model, TabItemConfig config) {
        insert(model, getModelCount(), config);
    }

    @Override
    public HandlerRegistration addBeforeCloseHandler(BeforeCloseHandler<M> handler) {
        return addHandler(handler, BeforeCloseEvent.getType());
    }

    @Override
    public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<M> handler) {
        return addHandler(handler, BeforeSelectionEvent.getType());
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<M> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<M> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    @Override
    public void forceLayout() {
        container.forceLayout();

    }

    public M getActiveModel() {
        return activeModel;
    }

    public TabPanel.TabPanelAppearance getAppearance() {
        return appearance;
    }

    /**
     * Returns true if scrolling is animated.
     *
     * @return the animation scroll state
     */
    public boolean getAnimScroll() {
        return animScroll;
    }

    /**
     * Returns true if the body border is enabled.
     *
     * @return the body border state
     */
    public boolean getBodyBorder() {
        return bodyBorder;
    }

    /**
     * Returns the tab item config for the given widget.
     *
     * @param model the widget
     * @return the config or null
     */
    public TabItemConfig getConfig(M model) {
        return configMap.get(model);
    }

    /**
     * Returns the internal card layout container.
     *
     * @return the card layout container
     */
    public SimpleContainer getContainer() {
        return container;
    }

    /**
     * Returns the tab panel messages.
     *
     * @return the messages
     */
    public TabPanel.TabPanelMessages getMessages() {
        if (messages == null) {
            messages = new DefaultTabPanelMessages();
        }
        return messages;
    }

    /**
     * Returns the minimum tab width.
     *
     * @return the minimum tab width
     */
    public int getMinTabWidth() {
        return minTabWidth;
    }

    /**
     * Returns true if tab resizing is enabled.
     *
     * @return the tab resizing state
     */
    public boolean getResizeTabs() {
        return resizeTabs;
    }

    /**
     * Returns the scroll duration in milliseconds.
     *
     * @return the duration
     */
    public int getScrollDuration() {
        return scrollDuration;
    }

    /**
     * Returns the panel's tab margin.
     *
     * @return the margin
     */
    public int getTabMargin() {
        return tabMargin;
    }

    /**
     * Returns true if tab scrolling is enabled.
     *
     * @return the tab scroll state
     */
    public boolean getTabScroll() {
        return tabScroll;
    }

    /**
     * Returns the default tab width.
     *
     * @return the width
     */
    public int getTabWidth() {
        return tabWidth;
    }

    public M getModel(int index) {
        return models.get(index);
    }

    public int getModelCount() {
        return models.size();
    }

    public int getModelIndex(M child) {
        return models.indexOf(child);
    }

    /**
     * Inserts the specified item into the tab panel.
     *
     * @param widget the item to insert
     * @param index the insert index
     * @param config the configuration of the tab item
     */
    public void insert(M widget, int index, TabItemConfig config) {
        configMap.put(widget, config);
        models.add(index, widget);
        appearance.insert(getElement(), config, index);

        if (getActiveModel() == null && autoSelect) {
            setActiveModel(widget);
        }

        if (getModelCount() == 1) {
            syncSize();
        }
    }

    /**
     * Returns true if auto select is enabled.
     *
     * @return the auto select state
     */
    public boolean isAutoSelect() {
        return autoSelect;
    }

    /**
     * Returns true if close context menu is enabled.
     *
     * @return the close menu state
     */
    public boolean isCloseContextMenu() {
        return closeMenu;
    }

    @Override
    public boolean isLayoutRunning() {
        return container.isLayoutRunning();
    }

    @Override
    public boolean isOrWasLayoutRunning() {
        return container.isOrWasLayoutRunning();
    }


    @Override
    public void onBrowserEvent(Event event) {
        XElement target = event.getEventTarget().cast();
        if (target == null) {
            return;
        }
        boolean isbar = appearance.getBar(getElement()).isOrHasChild(target);
        boolean orig = disableContextMenu;

        // allow right clicks in tab panel body
        if (!isbar && disableContextMenu) {
            disableContextMenu = false;
        }

        super.onBrowserEvent(event);

        if (!isbar) {
            disableContextMenu = orig;
            return;
        }

        Element item = findItem(target);
        if (item != null) {
            int index = itemIndex(item);
            if (index < 0) {
                // tab may have already closed
                return;
            }
            TabItemConfig config = getConfig(getModel(index));
            if (config != null && !config.isEnabled()) {
                return;
            }
        }

        switch (event.getTypeInt()) {
            case Event.ONCLICK:
                onClick(event);
                break;
            case Event.ONMOUSEOVER:
                appearance.onMouseOver(getElement(), event.getEventTarget().<XElement>cast());
                break;
            case Event.ONMOUSEOUT:

                appearance.onMouseOut(getElement(), event.getEventTarget().<XElement>cast());
                break;
        }
    }

    public boolean remove(int index) {
        return remove(getModel(index));
    }

    public boolean remove(M child) {
        int idx = getModelIndex(child);
        M activeModel = getActiveModel();
        boolean removed = models.remove(child);

        if (removed) {
            if (stack != null) {
                stack.remove(child);
            }

            Element item = findItem(idx);
            item.removeFromParent();

            if (child == activeModel) {
                M next = stack != null ? stack.next() : null;
                if (next != null) {
                    setActiveModel(next);
                } else if (getModelCount() > 0) {
                    setActiveModel(getModel(0));
                } else {
                    setActiveModel(null);
                }
            }
            delegateUpdates();
        }

        return removed;
    }

    /**
     * Scrolls to a particular tab if tab scrolling is enabled.
     *
     * @param item the item to scroll to
     * @param animate true to animate the scroll
     */
    public void scrollToTab(M item, boolean animate) {
        if (item == null) return;
        int pos = getScrollPos();
        int area = getScrollArea();
        XElement itemEl = findItem(getModelIndex(item)).cast();
        int left = itemEl.getOffsetsTo(getStripWrap()).getX() + pos;
        int right = left + itemEl.getOffsetWidth();
        if (left < pos) {
            scrollTo(left, animate);
        } else if (right > (pos + area)) {
            scrollTo(right - area, animate);
        }
    }

    /**
     * Sets the active widget.
     *
     * @param item the widget
     * @param fireEvents {@code true} to fire events
     */
    public void setActiveModel(M item, boolean fireEvents) {
        if (item == null) {
            return;
        }

        if (getActiveModel() != item) {
            if (fireEvents) {
                BeforeSelectionEvent<M> event = BeforeSelectionEvent.fire(this, item);
                // event can be null if no handlers
                if (event != null && event.isCanceled()) {
                    return;
                }
            }

            if (getActiveModel() != null) {
                appearance.onDeselect(findItem(getModelIndex(getActiveModel())));
            }
            appearance.onSelect(findItem(getModelIndex(item)));
            activeModel = item;

            if (stack == null) {
                stack = new AccessStack<M>();
            }
            stack.add(item);

            focusTab(item, false);
            if (fireEvents) {
                SelectionEvent.fire(this, item);
            }
            delegateUpdates();
        }
    }

    public void setActiveModel(M item) {
        setActiveModel(item, true);
    }

    /**
     * True to animate tab scrolling so that hidden tabs slide smoothly into view (defaults to true). Only applies when
     * {@link #tabScroll} = true.
     *
     * @param animScroll the animation scroll state
     */
    public void setAnimScroll(boolean animScroll) {
        this.animScroll = animScroll;
    }

    /**
     * True to have the first item selected when the panel is displayed for the first time if there is not selection
     * (defaults to true).
     *
     * @param autoSelect the auto select state
     */
    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    /**
     * True to display an interior border on the body element of the panel, false to hide it (defaults to true,
     * pre-render).
     *
     * @param bodyBorder the body border style
     */
    public void setBodyBorder(boolean bodyBorder) {
        this.bodyBorder = bodyBorder;
    }

    /**
     * True to show the close context menu (defaults to false).
     *
     * @param closeMenu true to show it
     */
    public void setCloseContextMenu(boolean closeMenu) {
        this.closeMenu = closeMenu;
        disableContextMenu = true;
        if (closeMenu) {
            sinkEvents(Event.ONCONTEXTMENU);
        }

        if (closeContextMenuGestureRecognizer == null) {
            closeContextMenuGestureRecognizer = new LongPressOrTapGestureRecognizer() {
                @Override
                protected void onLongPress(TouchData touchData) {
                    super.onLongPress(touchData);
                    onRightClick((Event) touchData.getLastNativeEvent());
                }

                @Override
                public boolean handleEnd(NativeEvent endEvent) {
                    // onRightClick does preventDefault and stopPropagation
                    cancel();
                    return super.handleEnd(endEvent);
                }
            };
            addGestureRecognizer(closeContextMenuGestureRecognizer);
        }
    }

    /**
     * Sets the tab panel messages.
     *
     * @param messages the messages
     */
    public void setMessages(TabPanel.TabPanelMessages messages) {
        this.messages = messages;
    }

    /**
     * The minimum width in pixels for each tab when {@link #resizeTabs} = true (defaults to 30).
     *
     * @param minTabWidth the minimum tab width
     */
    public void setMinTabWidth(int minTabWidth) {
        this.minTabWidth = minTabWidth;
    }

    /**
     * True to automatically resize tabs. The resize operation takes into consideration the current width of the tab panel
     * as well as the current values of {@link #setTabWidth(int)} and {@link #setMinTabWidth(int)}. The resulting tab
     * width will not be less than the value specified by <code>setMinTabWidth</code> nor greater than the value specified
     * by <code>setTabWidth</code>. To automatically resize the tabs to completely fill the tab strip, use
     * <code>setTabWidth(Integer.MAX_VALUE)</code> and <code>setResizeTabs(true)</code>.
     *
     * @param resizeTabs true to enable tab resizing
     */
    public void setResizeTabs(boolean resizeTabs) {
        this.resizeTabs = resizeTabs;
    }

    /**
     * Sets the number of milliseconds that each scroll animation should last (defaults to 150).
     *
     * @param scrollDuration the scroll duration
     */
    public void setScrollDuration(int scrollDuration) {
        this.scrollDuration = scrollDuration;
    }

    /**
     * Sets the number of pixels to scroll each time a tab scroll button is pressed (defaults to 100, or if
     * {@link #setResizeTabs(boolean)} = true, the calculated tab width). Only applies when {@link #setTabScroll(boolean)}
     * = true.
     *
     * @param scrollIncrement the scroll increment
     */
    public void setScrollIncrement(int scrollIncrement) {
        this.scrollIncrement = scrollIncrement;
    }

    /**
     * The number of pixels of space to calculate into the sizing and scrolling of tabs (defaults to 2).
     *
     * @param tabMargin the tab margin
     */
    public void setTabMargin(int tabMargin) {
        this.tabMargin = tabMargin;
    }

    /**
     * True to enable scrolling to tabs that may be invisible due to overflowing the overall TabPanel width. Only
     * available with tabs on top. (defaults to false).
     *
     * @param tabScroll true to enable tab scrolling
     */
    public void setTabScroll(boolean tabScroll) {
        this.tabScroll = tabScroll;
    }

    /**
     * Sets the initial width in pixels of each new tab (defaults to 120).
     *
     * @param tabWidth the tab width
     */
    public void setTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    /**
     * Updates the appearance of the specified tab item. Must be invoked after changing the tab item configuration.
     *
     * @param model the widget for the tab to update
     * @param config the new or updated tab item configuration
     */
    public void update(M model, TabItemConfig config) {
        XElement item = findItem(getModelIndex(model));
        if (item != null) {
            configMap.put(model, config);
            appearance.updateItem(item, config);
        }
    }

    protected void close(M item) {
        if (fireCancellableEvent(new BeforeCloseEvent<M>(item)) && remove(item)) {
            fireEvent(new CloseEvent<M>(item));
        }
    }

    @Override
    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(container);
    }

    @Override
    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(container);
    }

    protected Element findItem(Element target) {
        return target.<XElement> cast().findParentElement(appearance.getItemSelector(), -1);
    }

    protected XElement findItem(int index) {
        NodeList<Element> items = appearance.getStripWrap(getElement()).select(appearance.getItemSelector());
        return items.getItem(index).cast();
    }

    protected XElement getStripWrap() {
        return appearance.getStripWrap(getElement());
    }

    protected int itemIndex(Element item) {
        NodeList<Element> items = appearance.getStripWrap(getElement()).select(appearance.getItemSelector());
        for (int i = 0; i < items.getLength(); i++) {
            if (items.getItem(i) == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onAfterFirstAttach() {
        super.onAfterFirstAttach();

        if (!bodyBorder) {
            appearance.getBody(getElement()).getStyle().setProperty("border", "none");
        }

        getElement().setTabIndex(0);
        getElement().setAttribute("hideFocus", "true");

        sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS | Event.ONKEYUP | Event.FOCUSEVENTS);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        appearance.getBar(getElement()).disableTextSelection(true);

        if (getActiveModel() == null && autoSelect && getModelCount() > 0) {
            setActiveModel(getModel(0));
        }
    }

    @Override
    protected void onBlur(Event event) {
        super.onBlur(event);
    }

    protected void onClick(Event event) {
        XElement target = event.getEventTarget().cast();
        Element item = findItem(target);
        if (item != null) {
            event.stopPropagation();
            M w = getModel(itemIndex(item));
            boolean close = appearance.isClose(target);
            if (close) {
                close(w);
            } else if (w != getActiveModel()) {
                setActiveModel(w);
                focusTab(w, true);
            } else if (w == getActiveModel()) {
                focusTab(w, true);
            }
        }

        if (appearance.getScrollLeft(getElement()) != null
                && target.isOrHasChild(appearance.getScrollLeft(getElement()))) {
            event.stopPropagation();
            onScrollLeft();
        }
        if (appearance.getScrollRight(getElement()) != null
                && target.isOrHasChild(appearance.getScrollRight(getElement()))) {
            event.stopPropagation();
            onScrollRight();
        }
    }

    @Override
    protected void onDetach() {
        appearance.getBar(getElement()).disableTextSelection(false);
        super.onDetach();
    }

    @Override
    protected void onFocus(Event event) {
        if (getModelCount() > 0 && getActiveModel() == null) {
            setActiveModel(getModel(0));
        } else if (getActiveModel() != null) {
            focusTab(getActiveModel(), true);
        }
    }

    protected void onItemContextMenu(final M item, int x, int y) {
        if (closeMenu) {
            if (closeContextMenu == null) {
                closeContextMenu = new Menu();
                closeContextMenu.addHideHandler(new HideHandler() {

                    @Override
                    public void onHide(HideEvent event) {
                        contextMenuItem = null;
                    }
                });

                closeContextMenu.add(new MenuItem(getMessages().closeTab(), new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> event) {
                        close(contextMenuItem);
                    }
                }));

                closeContextMenu.add(new MenuItem(getMessages().closeOtherTabs(), new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> event) {
                        List<M> models = new ArrayList<M>();
                        for (int i = 0, len = getModelCount(); i < len; i++) {
                            models.add(getModel(i));
                        }

                        for (M w : models) {
                            TabItemConfig config = getConfig(w);
                            if (w != contextMenuItem && config.isClosable()) {
                                close(w);
                            }
                        }
                    }

                }));
            }
            TabItemConfig c = configMap.get(item);
            MenuItem mi = (MenuItem) closeContextMenu.getWidget(0);
            mi.setEnabled(c.isClosable());
            contextMenuItem = item;
            boolean hasClosable = false;

            for (int i = 0, len = getModelCount(); i < len; i++) {
                Widget item2 = container.getWidget(i);
                TabItemConfig config = configMap.get(item2);
                if (config.isClosable() && item2 != item) {
                    hasClosable = true;
                    break;
                }
            }
            MenuItem m = (MenuItem) closeContextMenu.getWidget(1);
            m.setEnabled(hasClosable);
            closeContextMenu.showAt(x, y);
        }
    }

    protected void onItemTextChange(Widget tabItem, String oldText, String newText) {
        delegateUpdates();
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        Size frameWidth = getElement().getFrameSize();

        if (!isAutoHeight()) {
            height -= frameWidth.getHeight() + appearance.getBar(getElement()).getOffsetHeight();
        }
        if (!isAutoWidth()) {
            width -= frameWidth.getWidth();
        }

        appearance.getBody(getElement()).setSize(width, height, true);
        appearance.getBar(getElement()).setWidth(width, true);

        if (!isAutoHeight()) {
            height -= appearance.getBody(getElement()).getFrameWidth(Side.TOP, Side.BOTTOM);
        }
        if (!isAutoWidth()) {
            width -= appearance.getBody(getElement()).getFrameWidth(Side.LEFT, Side.RIGHT);
        }
        container.setPixelSize(width, height);

        delegateUpdates();
    }

    @Override
    protected void onRightClick(Event event) {
        Element target = event.getEventTarget().cast();
        if (appearance.getBar(getElement()).isOrHasChild(target)) {
            Element item = findItem(event.getEventTarget().<Element> cast());
            if (item != null) {
                int idx = itemIndex(item);
                if (idx != -1) {
                    event.preventDefault();
                    event.stopPropagation();

                    Point point = event.<XEvent>cast().getXY();

                    final M w = getModel(idx);
                    final int x = point.getX();
                    final int y = point.getY();
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            onItemContextMenu(w, x, y);
                        }
                    });
                }
            }
        } else {
            super.onRightClick(event);
        }
    }

    protected void onTap(Event event) {
        XElement target = event.getEventTarget().cast();
        Element item = findItem(target);
        if (item != null) {
            int index = itemIndex(item);
            if (index < 0) {
                return;
            }
            TabItemConfig config = getConfig(getModel(index));
            if (config != null && !config.isEnabled()) {
                return;
            }
        }
        onClick(event);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (stack != null) {
            stack.clear();
        }
    }

    private void autoScrollTabs() {
        int count = getModelCount();
        int tw = appearance.getBar(getElement()).getClientWidth();
        if (tw == 0) {
            tw = getElement().getStyleSize().getWidth();
        }

        XElement stripWrap = appearance.getStripWrap(getElement());
        XElement edge = appearance.getStripEdge(getElement());
        XElement scrollLeft = appearance.getScrollLeft(getElement());
        XElement scrollRight = appearance.getScrollRight(getElement());

        int cw = stripWrap.getOffsetWidth();

        int pos = getScrollPos();
        int l = edge.<XElement> cast().getOffsetsTo(stripWrap).getX() + pos;

        if (!getTabScroll() || count < 1 || cw < 20) {
            return;
        }

        if (l <= tw) {
            stripWrap.<XElement> cast().setWidth(tw);
            if (scrolling) {
                stripWrap.setScrollLeft(0);
                scrolling = false;

                scrollLeft.setVisible(false);
                scrollRight.setVisible(false);

                // add a class that CSS can hook into to add/remove padding as necessary when scrollers change visibility
                scrollLeft.addClassName("x-tabScrollerLeftHidden");
                appearance.onScrolling(getElement(), false);
            }
        } else {
            if (!scrolling) {
                appearance.onScrolling(getElement(), true);
            }

            if (!scrolling) {
                if (scrollLeft == null) {
                    appearance.createScrollers(getElement());

                    // need to re-initialize scrollers as they will still be null
                    scrollLeft = appearance.getScrollLeft(getElement());
                    scrollRight = appearance.getScrollRight(getElement());
                } else {
                    scrollLeft.setVisible(true);
                    scrollRight.setVisible(true);
                    scrollLeft.removeClassName("x-tabScrollerLeftHidden");
                }
            }

            // account for scrollers before setting width
            tw -= scrollLeft.getComputedWidth() + scrollRight.getComputedWidth();
            stripWrap.<XElement> cast().setWidth(tw > 20 ? tw : 20);
            scrolling = true;
            if (pos > (l - tw)) {
                stripWrap.setScrollLeft(l - tw);
            } else {
                scrollToTab(getActiveModel(), false);
            }
            appearance.updateScrollButtons(getElement());
        }
    }

    private void autoSizeTabs() {
        int count = getModelCount();
        if (count == 0) return;
        int aw = appearance.getBar(getElement()).getClientWidth();
        if (aw == 0) {
            aw = getElement().getStyleSize().getWidth();
        }
        int each = (int) Math.max(Math.min(Math.floor((aw - 4) / count) - tabMargin, tabWidth), minTabWidth);

        for (int i = 0; i < count; i++) {
            XElement el = findItem(i).cast();
            appearance.setItemWidth(el, each);
        }
    }

    private void delegateUpdates() {
        if (!scheduledDelegateUpdates) {
            scheduledDelegateUpdates = true;
            Scheduler.get().scheduleFinally(new ScheduledCommand() {
                @Override
                public void execute() {
                    scheduledDelegateUpdates = false;
                    if (resizeTabs) {
                        autoSizeTabs();
                    }
                    if (tabScroll) {
                        autoScrollTabs();
                    }
                }
            });
        }
    }

    private void focusTab(M item, boolean setFocus) {
        if (setFocus) {
            // item.getHeader().getElement().focus();
        }
    }

    private int getScrollArea() {
        return Math.max(0, appearance.getStripWrap(getElement()).getClientWidth());
    }

    private int getScrollIncrement() {
        return scrollIncrement;
    }

    private int getScrollPos() {
        return appearance.getStripWrap(getElement()).getScrollLeft();
    }

    private int getScrollWidth() {
        return appearance.getStripEdge(getElement()).getOffsetsTo(getStripWrap()).getX() + getScrollPos();
    }

    private void onScrollLeft() {
        int pos = getScrollPos();
        int s = Math.max(0, pos - getScrollIncrement());
        if (s != pos) {
            scrollTo(s, getAnimScroll());
        }
    }

    private void onScrollRight() {
        int sw = getScrollWidth() - getScrollArea();
        int pos = getScrollPos();
        int s = Math.min(sw, pos + getScrollIncrement());
        if (s != pos) {
            scrollTo(s, getAnimScroll());
        }
    }

    private void scrollTo(int pos, boolean animate) {
        XElement stripWrap = getStripWrap();
        if (animate) {
            Fx fx = new Fx();
            fx.addAfterAnimateHandler(new AfterAnimateHandler() {
                @Override
                public void onAfterAnimate(AfterAnimateEvent event) {
                    appearance.updateScrollButtons(getElement());
                }
            });
            stripWrap.<FxElement> cast().scrollTo(ScrollDirection.LEFT, pos, true, fx);
        } else {
            stripWrap.setScrollLeft(pos);
            appearance.updateScrollButtons(getElement());
        }
    }
}
