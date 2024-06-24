import { ChangeDetectorRef, Component, HostListener, Renderer2 } from '@angular/core';
import { DialogService } from 'src/app//service/dialog.service';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { LoadingService } from './service/loading.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'cicd-form';
  isLoading = false;

  private inactivityTimer: any; // 用於儲存計時器的變量
  public logoutTime: number = 30;

  constructor(
    public dialogService: DialogService,
    private loadingService: LoadingService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private render2: Renderer2
  ) {
    this.initInactivityTimer();
    this.setupUpdater();
  }

  // 監聽屬標移動事件
  @HostListener('mousemove')
  @HostListener('mousedown')
  @HostListener('mouseup')
  // 監聽鍵盤输入事件
  @HostListener('keydown')
  @HostListener('keyup')
  // 監聽觸摸事件
  @HostListener('touchstart')
  @HostListener('touchend')
  resetUserActivity() {
    this.initInactivityTimer();
  }

  initInactivityTimer() {
    clearTimeout(this.inactivityTimer);
    this.logoutTime = 30;
    this.inactivityTimer = setTimeout(() => {
      //TIMEOUT 自動登出
      localStorage.clear();
      this.router.navigate(['logout']);
      this.dialogService.openMessageDialog("提示訊息", "超過30分鐘未使用，系統將自動登出");
    }, 1800000);
  }

  setupUpdater() {
    // 设置每分钟更新一次SessionStorage
    setInterval(() => {
      this.logoutTime = this.logoutTime - 1
    }, 60000);
  }

  ngOnInit() {
    this.loadingService.isLoading.subscribe((isLoading: boolean) => {
      this.isLoading = isLoading;
      this.cdr.detectChanges();
    });
  }
}
