// src/components/SwipeDeck.tsx
'use client';
import { interactWithJob } from '@/util/requests';
import { SwipeDirection, WithId } from '@/util/types';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';

type SwipeDeckProps<T extends WithId> = {
  items: T[];
  renderItem: (item: T) => React.ReactNode;
  onSwipe?: (dir: SwipeDirection, item: T) => void;
  height?: string | number;
  width?: string | number;
  thresholdPct?: number;
  emptyState?: React.ReactNode;
  controlsInside?: boolean;
  reservedBottom?: string | number;
};

const isInteractive = (el: EventTarget | null) =>
  el instanceof HTMLElement &&
  !!el.closest('button, a, input, textarea, select, label, [role="button"], [data-no-drag]');

export default function SwipeDeck<T extends WithId>({
  items,
  renderItem,
  onSwipe,
  height = '100%',
  width = 'min(52vw, 90%)',
  thresholdPct = 0.28,
  emptyState = <p className="text-sm text-gray-500">No more cards 🎉</p>,
  controlsInside = true,
  reservedBottom,
}: SwipeDeckProps<T>) {
  const [index, setIndex] = useState(0);
  const [dx, setDx] = useState(0);
  const [dy, setDy] = useState(0);
  const [dragging, setDragging] = useState(false);
  const [animating, setAnimating] = useState<'left' | 'right' | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  const wrapperRef = useRef<HTMLDivElement | null>(null);
  const overlayRef = useRef<HTMLDivElement | null>(null);
  const [reservedPx, setReservedPx] = useState(120);
  const [stackGap, setStackGap] = useState(14);

  const visible = useMemo(() => items.slice(index, index + 3), [items, index]);

  const topCardStyle = useMemo(() => {
    const tiltZ = Math.max(-18, Math.min(18, dx / 10));
    const tiltY = Math.max(-8, Math.min(8, dx / 30));
    const tiltX = Math.max(-6, Math.min(6, -dy / 40));
    return {
      transform: `translate(${dx}px, ${dy}px) rotate(${tiltZ}deg) rotateY(${tiltY}deg) rotateX(${tiltX}deg)`,
    } as React.CSSProperties;
  }, [dx, dy]);

  const commitSwipe = useCallback(
    async (dir: SwipeDirection) => {
      if (index >= items.length) return;
      const item = items[index];
      setAnimating(dir);

      interactWithJob(item.id, dir);

      const el = topEl.current;
      const offX = (dir === 'right' ? 1 : -1) * (window.innerWidth + 240);
      const rotate = dir === 'right' ? 28 : -28;
      if (el) {
        el.style.transition = 'transform 320ms cubic-bezier(.2,.7,.2,1)';
        el.style.transform = `translate(${offX}px, ${dy}px) rotate(${rotate}deg)`;
      }

      window.setTimeout(() => {
        setAnimating(null);
        setDx(0);
        setDy(0);
        setIndex((i) => i + 1);
        onSwipe?.(dir, item);
        setToast(dir === 'right' ? 'Saved' : 'Dismissed');
        window.setTimeout(() => setToast(null), 1100);
      }, 300);
    },
    [dy, index, items, onSwipe]
  );

  const topEl = useRef<HTMLDivElement | null>(null);
  const snapBack = () => {
    const el = topEl.current;
    if (!el) {
      setDx(0);
      setDy(0);
      return;
    }
    el.animate(
      [
        { transform: `translate(${dx}px, ${dy}px) rotate(${dx / 10}deg)` },
        { transform: 'translate(0px, 0px) rotate(0deg)' },
      ],
      { duration: 220, easing: 'cubic-bezier(.2,.7,.2,1)' }
    );
    setDx(0);
    setDy(0);
  };

  const startX = useRef(0),
    startY = useRef(0);
  const startedOnInteractive = useRef(false);
  const dragArmed = useRef(false);

  const onPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (animating) return;
    startedOnInteractive.current = isInteractive(e.target);
    dragArmed.current = false;
    startX.current = e.clientX;
    startY.current = e.clientY;
    e.currentTarget.setPointerCapture(e.pointerId);
    setDragging(true);
  };
  const onPointerMove = (e: React.PointerEvent) => {
    if (!dragging || animating) return;
    if (startedOnInteractive.current) return;
    const dxNow = e.clientX - startX.current;
    const dyNow = e.clientY - startY.current;
    if (!dragArmed.current) {
      if (Math.hypot(dxNow, dyNow) < 6) return;
      dragArmed.current = true;
    }
    setDx(dxNow);
    setDy(dyNow);
  };
  const onPointerUp = () => {
    if (!dragging || animating) return;
    setDragging(false);
    if (!dragArmed.current) {
      snapBack();
      return;
    }
    const rectW = topEl.current?.getBoundingClientRect().width ?? 1;
    const crossed = Math.abs(dx) > rectW * thresholdPct;
    if (crossed) {
      commitSwipe(dx > 0 ? 'right' : 'left');
    } else {
      snapBack();
    }
  };

  useEffect(() => {
    const update = () => {
      if (typeof reservedBottom === 'number') setReservedPx(reservedBottom);
      else if (typeof reservedBottom === 'string') {
        const px = reservedBottom.endsWith('px') ? parseFloat(reservedBottom) : NaN;
        if (!Number.isNaN(px)) setReservedPx(px);
      } else {
        const overlayH = overlayRef.current?.offsetHeight ?? 0;
        setReservedPx(Math.round(overlayH + 12));
      }

      const w = wrapperRef.current?.clientWidth ?? 640;
      setStackGap(Math.round(Math.max(8, Math.min(24, w * 0.02))));
    };

    update();
    const ro = new ResizeObserver(update);
    if (overlayRef.current) ro.observe(overlayRef.current);
    if (wrapperRef.current) ro.observe(wrapperRef.current);
    return () => ro.disconnect();
  }, [reservedBottom]);

  if (index >= items.length) {
    return (
      <div className="relative mx-auto" style={{ height, width }}>
        <div className="absolute inset-0 rounded-2xl border bg-white/95 p-6 text-center shadow-sm">
          {emptyState}
        </div>
      </div>
    );
  }

  const stackHeight = controlsInside ? `calc(100% - ${reservedPx}px)` : '100%';

  return (
    <div
      ref={wrapperRef}
      className="relative select-none overflow-visible mx-auto"
      style={{ height, width, perspective: 1200 }}
    >
      <div className="pointer-events-none absolute right-2 top-2 z-[140]">
        <span className="rounded-full bg-white/90 px-2 py-1 text-xs font-medium text-gray-900 shadow ring-1 ring-black/5">
          {index + 1} / {items.length}
        </span>
      </div>

      <div className="absolute inset-x-0 top-0" style={{ height: stackHeight }}>
        {visible.map((item, i) => {
          const isTop = i === 0;
          const scale = isTop ? 1 : 1 - i * 0.015;
          const y = isTop ? 0 : i * stackGap;
          const baseStyle: React.CSSProperties = isTop
            ? { ...topCardStyle }
            : { transform: `translateY(${y}px) scale(${scale})` };
          const style: React.CSSProperties = {
            ...baseStyle,
            zIndex: 100 - i,
            cursor: isTop ? (dragging ? 'grabbing' : 'grab') : 'default',
            pointerEvents: isTop ? 'auto' : 'none',
          };

          return (
            <div
              key={item.id}
              ref={isTop ? topEl : undefined}
              className="absolute inset-0 will-change-transform"
              style={style}
              onPointerDown={isTop ? onPointerDown : undefined}
              onPointerMove={isTop ? onPointerMove : undefined}
              onPointerUp={isTop ? onPointerUp : undefined}
              onPointerCancel={isTop ? onPointerUp : undefined}
              role={isTop ? 'group' : undefined}
              aria-roledescription={isTop ? 'Tinder-like swipable card' : undefined}
            >
              <div className="h-full w-full">{renderItem(item)}</div>
            </div>
          );
        })}
      </div>

      {/* Toast — never blocks clicks */}
      <div
        className={`pointer-events-none absolute left-1/2 -translate-x-1/2 transition-opacity duration-200 ${toast ? 'opacity-100' : 'opacity-0'
          } z-[200]`}
        style={{ bottom: reservedPx + 36 }}
        aria-live="polite"
      >
        <div className="rounded-full bg-gray-900/90 px-3 py-1.5 text-xs text-white shadow-lg">
          {toast ?? ' '}
        </div>
      </div>
    </div>
  );
}
