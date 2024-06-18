import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { ActionInfo } from 'src/app/bean/action-info';
import { ColumnData } from 'src/app/bean/form-data';
import { DATA_STATUS_ENUM } from 'src/app/enum/DATA_STATUS_ENUM';
import { URL_ENUM } from 'src/app/enum/URL_ENUM';
import { RestApiService } from 'src/app/service/rest-api.service';
import { v4 as uuidv4 } from 'uuid';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-cell-file-upload',
  templateUrl: './cell-file-upload.component.html',
  styleUrls: ['./cell-file-upload.component.css']
})
export class CellFileUploadComponent {
  DATA_STATUS_ENUM = DATA_STATUS_ENUM;
  private _columnData!: ColumnData;
  public _disabled: boolean = false;
  public actionInfo: ActionInfo = new ActionInfo();
  public status: string = DATA_STATUS_ENUM.UNUPLOAD;

  constructor(public restApi: RestApiService) { }

  ngOnInit(): void {
    this.actionInfo.userId = localStorage.getItem('userId');
    this.actionInfo.usrName = localStorage.getItem('usrName');
    this.actionInfo.visitorName = localStorage.getItem('visitorName');
  }

  @Input()
  set columnData(columnData: ColumnData) {
    this._columnData = columnData;
    if (columnData.colValue && columnData.colValue != null && columnData.colValue != "")
      this.status = DATA_STATUS_ENUM.UPLOADED
  }
  get columnData(): ColumnData {
    return this._columnData;
  }

  @Input()
  set disabled(disabled: boolean) {
    this._disabled = disabled;
  }
  get disabled(): boolean {
    return this._disabled;
  }

  @Output()
  columnDataChange = new EventEmitter<ColumnData>();
  onSetColValue(): void {
    this.columnDataChange.emit(this._columnData);
  }

  onFileSelected(event: any) {
    const selectedFile: File = event.target.files[0];
    this.uploadFiles(selectedFile).subscribe((fileUuid: string) => {
      if (!fileUuid || fileUuid == "" || fileUuid == null) {
        event.target.value = null;
      } else {
        this._columnData.colValue = fileUuid;
        this._columnData.colOptions = selectedFile.name;
        this.status = DATA_STATUS_ENUM.UPLOADED
      }
    });
  }


  uploadFiles(file: File): Observable<string> {
    const formData = new FormData();

    // 寫入 TID 供錯誤訊息、後端 LOG 使用
    const tid = uuidv4();
    this.actionInfo.tid = tid;

    // 添加 actionInfo 到 FormData 中
    formData.append('actionInfo', JSON.stringify(this.actionInfo));

    // 將檔案添加到 FormData
    if (file) {
      formData.append('file', file);
    }

    return this.restApi.upload(URL_ENUM.ATTACH_FILE_CONTROLLER.UPLOAD_FILE, formData);
  }

  downloadFile(uuid: string | undefined) {
    this.restApi.post(URL_ENUM.ATTACH_FILE_CONTROLLER.DOWNLOAD_FILE,
      {
        actionInfo: this.actionInfo,
        body: uuid
      },
      true
    ).subscribe((response: any) => {
      const blob = new Blob([response], { type: 'application/octet-stream' });

      if (this._columnData.colOptions) {
        saveAs(blob, this._columnData.colOptions);
      }
    });
  }

  delete(uuid: string | undefined) {
    this.restApi.post(URL_ENUM.ATTACH_FILE_CONTROLLER.DELETE_FILE,
      {
        actionInfo: this.actionInfo,
        body: uuid
      }
    ).subscribe(() => {
      this._columnData.colValue = undefined;
      this._columnData.colOptions = undefined;
      this.status = DATA_STATUS_ENUM.UNUPLOAD
    });
    this.onSetColValue();
  }
}
