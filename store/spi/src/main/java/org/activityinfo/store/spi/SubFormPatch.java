package org.activityinfo.store.spi;

import com.google.common.base.Optional;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodType;

public class SubFormPatch {

  public static final ResourceId PERIOD_FIELD_ID = ResourceId.valueOf("period");

  public static FormClass patch(FormClass formClass) {
    if(formClass.isSubForm()) {
      if(formClass.getSubFormKind().isPeriod()) {
        return ensureSubFormHasPeriodField(formClass);
      }
    }
    return formClass;
  }

  private static FormClass ensureSubFormHasPeriodField(FormClass formClass) {
    Optional<FormField> period = formClass.getFieldIfPresent(PERIOD_FIELD_ID);
    if(period.isPresent()) {
      return formClass;
    }

    // Add period field
    FormField periodField = new FormField(ResourceId.valueOf("period"));
    periodField.setLabel(fieldName(formClass.getSubFormKind()));
    periodField.setType(formClass.getSubFormKind().getPeriodType());
    periodField.setKey(true);
    periodField.setRequired(true);
    periodField.setVisible(true);

    formClass.getElements().add(0, periodField);

    return formClass;
  }

  private static String fieldName(SubFormKind kind) {
    switch (kind) {
      case MONTHLY:
        return I18N.CONSTANTS.month();
      case WEEKLY:
        return I18N.CONSTANTS.week();
      case BIWEEKLY:
        return I18N.CONSTANTS.fortnight();
      case DAILY:
      default:
        return I18N.CONSTANTS.date();
    }
  }

  public static CursorObserver<ResourceId> fromRecordId(FormClass formClass, final CursorObserver<FieldValue> observer) {
    final ResourceId formId = formClass.getId();
    final PeriodType periodType = formClass.getSubFormKind().getPeriodType();

    return new CursorObserver<ResourceId>() {
      @Override
      public void onNext(ResourceId value) {
        observer.onNext(periodType.fromSubFormKey(new RecordRef(formId, value)));
      }

      @Override
      public void done() {
        observer.done();
      }
    };
  }
}
