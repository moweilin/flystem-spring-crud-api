package com.flystem.crud.api.guest;

import java.util.UUID;

/** Guest 值类型为 {@link UUID} 的公开 marker（{@link GuestId} sealed 体系入口）。 */
public non-sealed interface UUIDGuestId extends GuestId<UUID> {
}
