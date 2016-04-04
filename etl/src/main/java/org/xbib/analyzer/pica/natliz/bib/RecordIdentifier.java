package org.xbib.analyzer.pica.natliz.bib;

import org.xbib.etl.marc.dialects.pica.PicaEntity;
import org.xbib.etl.marc.dialects.pica.PicaEntityQueue;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;

public class RecordIdentifier extends PicaEntity {

    @Override
    public void fields(PicaEntityQueue.PicaKeyValueWorker worker, FieldList fields) {
        for (Field field : fields) {
            worker.state().setRecordNumber(field.data());
        }
    }
}
