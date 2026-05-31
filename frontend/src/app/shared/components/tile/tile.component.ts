import { Component, computed, input, output } from '@angular/core';

export type TileType = 'default' | 'success' | 'warning' | 'danger' | 'info';

@Component({
  selector: 'app-tile',
  standalone: true,
  templateUrl: './tile.component.html',
  styleUrl: './tile.component.scss',
})
export class TileComponent {
  readonly title      = input.required<string>();
  readonly count      = input.required<number | string>();
  readonly subtitle   = input<string>();
  readonly clickable  = input(false);
  readonly compact    = input(false);
  readonly labelColor = input<string>();
  readonly type       = input<TileType>('default');

  readonly tileClick = output<void>();

  protected readonly tileClasses = computed(() => [
    `tile--${this.type()}`,
    this.clickable() ? 'tile--clickable' : 'tile--disabled',
    this.compact()   ? 'tile--compact'   : '',
  ].filter(Boolean).join(' '));

  protected handleClick(): void {
    if (this.clickable()) {
      this.tileClick.emit();
    }
  }
}
