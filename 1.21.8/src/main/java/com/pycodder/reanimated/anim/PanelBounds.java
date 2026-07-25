package com.pycodder.reanimated.anim;

/**
 * Доступ к вертикальным границам панели контейнерного экрана (верх/низ в экранных
 * координатах). Реализуется миксином на {@code HandledScreen}, где живут поля панели;
 * другие миксины (напр. вкладки креатива) получают границы через приведение к этому
 * интерфейсу — прямой {@code @Shadow} унаследованных полей в подклассе не резолвится.
 */
public interface PanelBounds {
    int reanimated$panelTop();
    int reanimated$panelBottom();
}
