package org.activityinfo.ui.client.widget;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import org.activityinfo.ui.client.util.GwtUtil;

/**
 * @author yuriyz on 4/7/14.
 */
public class CellTable<T> extends com.google.gwt.user.cellview.client.CellTable<T> {

    public static interface ScrollHandler extends EventHandler {

        void onScroll(ScrollEvent event);
    }

    public static class ScrollEvent extends GwtEvent<ScrollHandler> {

        public static final Type<ScrollHandler> TYPE = new Type<>();

        private final ScrollPanel scrollAncestor;

        public ScrollEvent(ScrollPanel scrollAncestor) {
            this.scrollAncestor = scrollAncestor;
        }

        public int getVerticalScrollPosition() {
            if (scrollAncestor != null) {
                return scrollAncestor.getVerticalScrollPosition();
            } else {
                return Window.getScrollTop();
            }
        }

        public ScrollPanel getScrollAncestor() {
            return scrollAncestor;
        }

        @Override
        public Type<ScrollHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ScrollHandler handler) {
            handler.onScroll(this);
        }
    }

    private final EventBus eventBus = new SimpleEventBus();
    private CellTableAffixer affixer;

    public CellTable(int pageSize, Resources resources) {
        super(pageSize, resources);
        addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            affixer = new CellTableAffixer(CellTable.this);
                            final SelectionModel<? super T> selectionModel = CellTable.this.getSelectionModel();
                            if (selectionModel != null) {
                                selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                                    @Override
                                    public void onSelectionChange(SelectionChangeEvent event) {
                                        // AI-535 : Affixed table headers don't work in IE10
                                        affixer.forceAffix();
                                    }
                                });
                            }
                        }
                    });
                    addScrollHandlers();
                }
            }
        });
    }

    private void addScrollHandlers() {
        final ScrollPanel scrollAncestor = getScrollAncestor();
        if (scrollAncestor != null) {
            scrollAncestor.addScrollHandler(new com.google.gwt.event.dom.client.ScrollHandler() {
                @Override
                public void onScroll(com.google.gwt.event.dom.client.ScrollEvent event) {
                    eventBus.fireEvent(new ScrollEvent(scrollAncestor));
                }
            });
        } else { // attach scroll handler to window (if scrollAncestor can't be identified)
            Window.addWindowScrollHandler(new Window.ScrollHandler() {
                @Override
                public void onWindowScroll(Window.ScrollEvent event) {
                    eventBus.fireEvent(new ScrollEvent(scrollAncestor));
                }
            });
        }
    }

    @Override
    public TableSectionElement getTableHeadElement() {
        return super.getTableHeadElement();
    }

    public CellTableAffixer getAffixer() {
        return affixer;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ScrollPanel getScrollAncestor() {
        return GwtUtil.getScrollAncestor(this);
    }

    public void saveColumnWidthInformation() {
        if (affixer != null) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    affixer.getWidthApplier().saveHeaderWidthInformation();
                }
            });
        }
    }

}
