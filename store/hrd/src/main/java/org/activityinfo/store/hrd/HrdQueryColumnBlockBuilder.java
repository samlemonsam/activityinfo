package org.activityinfo.store.hrd;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.columns.*;
import org.activityinfo.store.hrd.entity.FieldDescriptor;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.spi.ColumnQueryBuilderV2;
import org.activityinfo.store.spi.FieldComponent;
import org.activityinfo.store.spi.PendingSlot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HrdQueryColumnBlockBuilder implements ColumnQueryBuilderV2 {

    private static final Logger LOGGER = Logger.getLogger(HrdQueryColumnBuilder.class.getName());

    private final FormEntity formEntity;
    private final FormClass formClass;
    private Multimap<FieldComponent, PendingSlot<ColumnView>> fieldTargets = HashMultimap.create();
    private Set<ResourceId> fields = new HashSet<>();
    private List<PendingSlot<ColumnView>> idTargets = new ArrayList<>();
    private List<PendingSlot<ColumnView>> parentIdTargets = new ArrayList<>();
    private List<PendingSlot<Integer>> rowCountTargets = new ArrayList<>();

    public HrdQueryColumnBlockBuilder(FormEntity formEntity, FormClass formClass) {
        this.formEntity = formEntity;
        this.formClass = formClass;
    }

    @Override
    public void addRecordId(PendingSlot<ColumnView> target) {
        idTargets.add(target);
    }

    @Override
    public void addRowCount(PendingSlot<Integer> rowCount) {
        rowCountTargets.add(rowCount);
    }

    @Override
    public void addField(FieldComponent fieldComponent, PendingSlot<ColumnView> target) {
        if(fieldComponent.getFieldId().asString().equals("@parent")) {
            parentIdTargets.add(target);
        } else {
            fieldTargets.put(fieldComponent, target);
            fields.add(fieldComponent.getFieldId());
        }
    }

    @Override
    public void execute() {


        // Provide row counts
        for (PendingSlot<Integer> rowCountTarget : rowCountTargets) {
            rowCountTarget.set(formEntity.getRecordCount());
        }

        BlockResolver blockResolver = new BlockResolver(null, formEntity);
        blockResolver.fetchRecordIds();
        blockResolver.fetchTombstones();

        if(!parentIdTargets.isEmpty()) {
            blockResolver.fetchParentIds();
        }

        // Determine which column-blocks we need to fetch for this query
        Set<String> columnBlocks = new HashSet<>();
        for (ResourceId field : fields) {
            FieldDescriptor fieldDescriptor = formEntity.getFieldDescriptor(field.asString());
            if(fieldDescriptor.hasBlockAssignment()) {
                columnBlocks.add(fieldDescriptor.getColumnId());
            }
        }

        for (String columnBlock : columnBlocks) {
            blockResolver.fetchFieldBlock(columnBlock);
        }

        blockResolver.load();

        // Start caching in background, while we are building columns
        Future<Void> caching = blockResolver.cacheBlocks();

        // Now construct column views from blocks

        TombstoneIndex tombstoneIndex = new TombstoneIndex(formEntity, blockResolver.getTombstoneBlocks());

        if(!idTargets.isEmpty()) {
            BlockManager blockManager = new RecordIdBlock();
            ColumnView columnView = blockManager.buildView(formEntity, tombstoneIndex,
                    blockResolver.getBlocks(RecordIdBlock.BLOCK_NAME));

            for (PendingSlot<ColumnView> idTarget : idTargets) {
                idTarget.set(columnView);
            }
        }

        if(!parentIdTargets.isEmpty()) {
            BlockManager blockManager = BlockFactory.forParentId();
            ColumnView columnView = blockManager.buildView(formEntity, tombstoneIndex,
                    blockResolver.getBlocks(ColumnModel.PARENT_SYMBOL));

            for (PendingSlot<ColumnView> parentIdTarget : parentIdTargets) {
                parentIdTarget.set(columnView);
            }
        }

        for (FieldComponent fieldComponent : fieldTargets.keySet()) {
            ColumnView columnView = buildFieldColumnView(blockResolver, tombstoneIndex, fieldComponent);

            for (PendingSlot<ColumnView> fieldTarget : fieldTargets.get(fieldComponent)) {
                fieldTarget.set(columnView);
            }
        }

        try {
            caching.get();
        } catch (Exception e ){
            LOGGER.log(Level.SEVERE, "Failed to cache fetched blocks", e);
        }
    }

    private ColumnView buildFieldColumnView(BlockResolver blockResolver, TombstoneIndex tombstoneIndex, FieldComponent fieldComponent) {
        try {

            FormField field = formClass.getField(fieldComponent.getFieldId());
            FieldDescriptor descriptor = formEntity.getFieldDescriptor(field.getName());
            BlockManager blockManager = BlockFactory.get(field);

            return blockManager.buildView(formEntity, tombstoneIndex,
                    blockResolver.getBlocks(descriptor),
                    fieldComponent.getComponent());


        } catch (Exception e) {
            throw new RuntimeException("Exception building view from " + fieldComponent, e);
        }
    }
}
